<?php
require_once 'auth.php';
require_once 'tmdbposter.php';
require_once 'models.php';

$request = explode('/', $_GET['request']);

if ($request[0] == "movies") {
  // default zip code
  $zip = 15213;

  print file_get_contents('mock/movies');
  exit(1);

  $result = json_decode(file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . ONCONNECT_KEY));
  //print_r($result);
  //exit(1);

  $movies = array();
  foreach ($result as $data) {
    
    // group movies by rootID which will inevitable collapse 3D/IMAX experiences accordingly
    if (!array_key_exists($data->rootId, $movies)) {
      $movie = new Movie();
      $movie->id = $data->tmsId; // this will probably be our database's ID
      $movie->tmsid = $data->tmsId;
      $movie->title = $data->title;
      $movie->description = $data->longDescription;
      $movie->rating = $data->ratings[0]->code;
      $movie->genres = $data->genres;

      // parse runtime "PT02H14M" --> "2 hr 14 min"
      preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
      $movie->runtime = '';
      $hours = intval($runtime[1]);
      if ($hours !== 0) {
        // don't include hours if they are 0
        $movie->runtime .= $hours.' hr ';
      }
      $movie->runtime .= intval($runtime[2]).' min';

      // add poster via the movie database
      $tmdb = new TMDBposter();
      $tmdb_results = $tmdb->searchMovie($movie->title,'en');
      $movie->poster = '';
      if (isset($tmdb_results['results'][0]['poster_path'])) {
        $movie->poster = 'http://image.tmdb.org/t/p/w500'.$tmdb_results['results'][0]['poster_path'];
      }
    } else {
      $movie = $movies[$data->rootId];
    }

    // group showtimes by theater
    foreach ($data->showtimes as $showtime) {
      $id = $showtime->theatre->id;

      // add new theater if it does not already exist
      if (!array_key_exists($id, $movie->theaters)) {
        $theater = new Theater($id, $showtime->theatre->name);
        if (isset($showtime->ticketURI)) {
          $theater->ticketurl = $showtime->ticketURI;
        }
        $movie->theaters[$id] = $theater;
      }

      // parse showtime "2014-03-14T13:10" --> "2014-03-14 13:10"
      $time = new Showtime(str_replace('T', ' ', $showtime->dateTime));

      // check if showtime is 3D or IMAX experience
      if (isset($showtime->quals)) {
        if (strpos($showtime->quals, '3D')) {
          $time->flag = FLAG_3D;
        } else if (strpos($showtime->quals, 'IMAX')) {
          $time->flag = FLAG_IMAX;
        }
      }

      // add showtime to appropriate theater object
      // @todo make sure these are sorted chronologically
      array_push($movie->theaters[$id]->showtimes, $time);
    }
    
    $movies[$data->rootId] = $movie;
  }

  // go back and sort showtimes (despite that this is really inefficient)
  foreach ($movies as $movie) {
    foreach ($movie->theaters as $theater) {
      usort($theater->showtimes, array('Showtime', 'cmp'));
    }
  }

  //print_r($movies);
  print json_encode($movies);

} else if ($request[0] == "friends") {
      
  $friends = array();
  $data = explode("\n", file_get_contents('mock/data/users.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    $friend = new User($line[0], $line[1]);
    $friend->photo = $line[2];
    array_push($friends, $friend);
  }
  
  print json_encode($friends);

} else if ($request[0] == "events") {

  $events = array();
  $data = explode("\n", file_get_contents('mock/data/events.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    $event = new Event($line[0]);
    $event->movie = new Movie($line[1]);
    $event->showtime = new Showtime($line[2]);
    $event->status = $line[3];
    for ($i = 0; $i < $line[4]; $i++) {
      array_push($event->guests, new Guest());
    }
    array_push($events, $event);
  }

  print json_encode($events);
}
