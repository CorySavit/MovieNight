<?php
require_once '../shared.php';

// setup database
$db = new medoo(DB_NAME);

// today's date for the time being
$date = date("Y-m-d");

//$result = json_decode(file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate='.date("Y-m-d").'&lat='.STATIC_LAT.'&lng='.STATIC_LNG.'&radius='.RADIUS_MILES.'&api_key=' . ONCONNECT_KEY));
$result = json_decode(file_get_contents('../mock/movies'));

foreach ($result as $data) {

  // see if movie already exists in database
  $movie = $db->get('movies', array(
    'id',
    'start_date',
    'end_date'
  ), array('tms_id' => $data->rootId));

  if (empty($movie)) {
    // movie does not already exist in database

    $tmdb = new TMDB($data->title, $data->releaseYear);

    // try to get runtime from tmdb first
    $myRuntime = $tmdb->getRuntime();
    if (is_null($myRuntime)) {
      // parse runtime "PT02H14M" --> "2 hr 14 min"
      preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
      $myRuntime = intval($runtime[1]) * 60 + intval($runtime[2]);
    }

    myLog("\ninsert movie \"".$data->title."\" into database");
    $movie_id = $db->insert('movies', array(
      'tms_id' => $data->rootId,
      'tmdb_id' => $tmdb->getTMDBId(),
      'imdb_id' => $tmdb->getIMDBId(),
      'title' => $data->title,
      'description' => is_null($tmdb->getOverview()) ? $data->description : $tmdb->getOverview(),
      'mpaa_rating' => $data->ratings[0]->code,
      'poster' => $tmdb->getPosterURL(),
      'backdrop' => $tmdb->getBackdropURL(),
      'runtime' => $myRuntime,
      'start_date' => $data->releaseDate,
      'end_date' => $date
    ));

    // add genres
    $genres = is_null($tmdb->getGenres()) ? $data->genres : $tmdb->getGenres();
    foreach ($genres as $genre) {

      if (gettype($genre) === "array") {
        // see if tmdb entry is already in database
        // @todo should probably search where 'name' as well
        $genre['tmdb_id'] = $genre['id'];
        unset($genre['id']);
        $genre_id = $db->get('genres', 'id', array('tmdb_id' => $genre['tmdb_id']));
      } else {
        // search for existing name in database
        $genre = array('name' => $genre);
        $genre_id = $db->get('genres', 'id', array('name' => $genre['name']));
      }

      if (empty($genre_id)) {
        // insert new genre if it doesn't exist
        myLog("insert genre \"".$genre['name']."\" into database");
        $genre_id = $db->insert('genres', $genre);
      }

      // create genre relationship
      myLog("add genre \"".$genre['name']."\" to movie");
      $db->insert('movies2genres', array(
        'movie_id' => $movie_id,
        'genre_id' => $genre_id
      ));

    }

    // @todo add cast


  } else {
    // movie already exists in database
    $movie_id = $movie['id'];
    myLog("\nmovie \"".$movie_id."\" already exists in database.");

    // check if we need to expand date interval
    // note that the start (release) date never changes
    if ($date > $movie['end_date']) {

      // update end date
      myLog("update movie_".$movie['id']." end_date to ".$date);
      $db->update('movies', array(
        'end_date' => $date
      ), array('id' => $movie['id']));

    }

    // @todo update things like rating again -- or move that into separate script?

  }

  // grab the close theaters from our database (only do this once for each movie)
  // uses Haversine formula
  // see https://developers.google.com/maps/articles/phpsqlsearch_v3#findnearsql
  $theaters = $db->query("SELECT
      id, google_id, name, (
        3959 * acos(
          cos(radians(".STATIC_LAT."))
          * cos(radians(lat))
          * cos(radians(lng) - radians(".STATIC_LNG."))
          + sin(radians(".STATIC_LAT."))
          * sin(radians(lat))
        )
    ) AS distance
    FROM theaters
    HAVING distance <= 31
    ORDER BY distance;")->fetchAll();

  // add showtimes (even if the movie is already in the database)
  addShowtimes($data->showtimes, $movie_id);

}

function addShowtimes($showtimes, $movie_id) {
  global $db, $theaters;
  
  foreach ($showtimes as $showtime) {

    $theater_id = $db->get('theaters', 'id', array('tms_id' => $showtime->theatre->id));
    if (!$theater_id) {
      
      // traverse through the close theaters in our database
      foreach ($theaters as $theater) {
        if ($theater['name'] == $showtime->theatre->name) {
          $theater_id = $theater['id'];
        }
      }

      if ($theater_id) {
        // theater already exists in the database (with same name)
        // update tmd_id accordingly
        myLog('update tms_id of theater "'.$theater_id.'"');
        $db->update('theaters',
          array('tms_id' => $showtime ->theatre->id),
          array('id' => $theater_id)
        );
      } else {
        // theater does not exist in the database
        // do a google places location search and only look at first result
        $nearbysearch = json_decode(file_get_contents('https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='.STATIC_LAT.','.STATIC_LNG.'&radius='.RADIUS_METERS.'&keyword='.urlencode($showtime->theatre->name).'&sensor=false&key='.PLACES_KEY));
        if ($nearbysearch->status == 'OK') {
          
          $nearbysearch = $nearbysearch->results[0];
          foreach ($theaters as $theater) {
            if ($theater['google_id'] == $nearbysearch->id) {
              // if this google_id matches one in our database, then update the tms_id accordingly
              $theater_id = $theater['id'];
              myLog('update tms_id of theater "'.$theater_id.'" via nearbysearch query');
              $db->update('theaters',
                array('tms_id' => $showtime ->theatre->id),
                array('id' => $theater_id)
              );
            }
          }

          if (!$theater_id) {
            // if theater does not exist in our database
            // add new theater with results of google places nearbysearch
            // note that we are still using TMS theater name with google places geographic data
            myLog("insert theater \"".$showtime->theatre->name."\" into database via nearbysearch query");
            $theater_id = $db->insert('theaters', array(
              'tms_id' => $showtime->theatre->id,
              'google_id' => $nearbysearch->id,
              'name' => $showtime->theatre->name,
              'address' => $nearbysearch->vicinity,
              'lat' => $nearbysearch->geometry->location->lat,
              'lng' => $nearbysearch->geometry->location->lng
            ));
          }

        } else {

          // as a last resort, we insert a theater with queried location data
          // @todo should we be even setting the lat/lng?
          myLog("insert theater \"".$showtime->theatre->name."\" into database");
          $theater_id = $db->insert('theaters', array(
            'tms_id' => $showtime->theatre->id,
            'name' => $showtime->theatre->name,
            'lat' => STATIC_LAT,
            'lng' => STATIC_LNG
          ));

        }
      }

    }

    // parse showtime "2014-03-14T13:10" --> "2014-03-14 13:10"
    $time = str_replace('T', ' ', $showtime->dateTime);

    // check if showtime is 3D or IMAX experience and set flag accordingly
    $flag = 0;
    if (isset($showtime->quals)) {
      if (strpos($showtime->quals, '3D')) {
        $flag = FLAG_3D;
      } else if (strpos($showtime->quals, 'IMAX')) {
        $flag = FLAG_IMAX;
      }
    }

    // see if showtime already exists in database
    $showtime_id = $db->get('showtimes', 'id', array(
      'AND' => array (
        'movie_id' => $movie_id,
        'theater_id' => $theater_id,
        'time' => $time,
        'flag' => $flag
      )
    ));

    if (empty($showtime_id)) {
      // add showtime to database
      myLog("insert showtime \"".$time."\" into database");
      $ticket_url = isset($showtime->ticketURI) ? $showtime->ticketURI : null;
      $db->insert('showtimes', array(
        'movie_id' => $movie_id,
        'theater_id' => $theater_id,
        'time' => $time,
        'flag' => $flag,
        'ticket_url' => $ticket_url
      ));
    }

  }

}

function myLog($output) {
  if (DEBUG) {
    echo $output."\n";
  }
}
