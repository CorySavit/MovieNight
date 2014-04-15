<?php

require_once 'auth.php';
require_once 'tmdb.php';

// setup database
require_once 'medoo.php';
$db = new medoo(DB_NAME);
define("INVALID_REQUEST", "Invalid Request");

$date = date("Y-m-d");

//$zip = 15213;
//echo file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . ONCONNECT_KEY);
$result = json_decode(file_get_contents('mock/tms'));

//$movies = array();
foreach ($result as $data) {

  // see if movie already exists in database
  $movie = $db->get('movies', array(
    'id',
    'start_date',
    'end_date'
  ), array('tms_id' => $data->rootId));

  if ($movie) {
    // movie already exists in database

    // check if we need to expand date interval
    $update = false;
    if ($date < $movie['start_date']) {

      // update start date
      $db->update('movies', array(
        'start_date' => $date
      ), array('id' => $movie['id']));
      $update = true;

    } else if ($date > $movie['end_date']) {

      // update end date
      $db->update('movies', array(
        'end_date' => $date
      ), array('id' => $movie['id']));
      $update = true;

    }

    // if we updated
    if ($update) {
      // do stuff?
    }

    // add showtimes (if they are not already in database)
    addShowtimes($data->showtimes, $movie['id']);

  } else {
    // movie does not already exist in database

    $tmdb = new TMDB($data->title, $data->releaseYear);

    // parse runtime "PT02H14M" --> "2 hr 14 min"
    preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
    $myRuntime = '';
    $hours = intval($runtime[1]);
    if ($hours !== 0) {
      // don't include hours if they are 0
      $myRuntime .= $hours.' hr ';
    }
    $myRuntime .= intval($runtime[2]).' min';

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
      'start_date' => $date,
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
        $genre_id = $db->select('genres', 'id', array('tmdb_id' => $genre['tmdb_id']));
      } else {
        // search for existing name in database
        $genre = array('name' => $genre);
        $genre_id = $db->select('genres', 'id', array('name' => $genre['name']));
      }

      if (!$genre_id) {
        // insert new genre if it doesn't exist
        $genre_id = $db->insert('genres', $genre);
      }

      // create genre relationship
      $db->insert('movies2genres', array(
        'movie_id' => $movie_id,
        'genre_id' => $genre_id
      ));

    }

    // add showtimes
    addShowtimes($data->showtimes, $movie_id);

    // @todo add cast

  }

  exit(1);

}

function addShowtimes($showtimes, $movie_id) {
  global $db;
  
  foreach ($showtimes as $showtime) {

    $theater_id = $db->select('theaters', 'id', array('tms_id' => $showtime->theatre->id));
    if (!$theater_id) {
      // @todo find location via google api
      $theater_id = $db->insert('theaters', array(
        'tms_id' => $showtime->theatre->id,
        'name' => $showtime->theatre->name
      ));
    } else {
      $theater_id = $theater_id[0];
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
    $showtime_id = $db->select('showtimes', 'id', array(
      'AND' => array (
        'movie_id' => $movie_id,
        'theater_id' => $theater_id,
        'time' => $time,
        'flag' => $flag
      )
    ));

    print ($movie_id."\n".$theater_id."\n".$time."\n".$flag."\n");
    print_r($showtime_id);

    if (!$showtime_id) {
      // add showtime to database
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
