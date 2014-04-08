<?php
header('Content-type: application/json');

require_once 'auth.php';
require_once 'tmdb.php';
require_once 'models.php';

$request = explode('/', $_GET['request']);

if ($request[0] == "movies") {
  // default zip code
  $zip = 15213;

  print file_get_contents('mock/movies');
  exit(1);

  // get our list of movies from TMS API
  $result = json_decode(file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . ONCONNECT_KEY));
  //print_r($result);
  //exit(1);

  $movies = array();
  foreach ($result as $data) {
    
    // group movies by rootID which will inevitable collapse 3D/IMAX experiences accordingly
    if (!array_key_exists($data->rootId, $movies)) {
      // find movie in The Movie Database
      $tmdb = new TMDB($data->title, $data->releaseYear);
      $movie = new Movie();

      $movie->id = $data->tmsId; // this will probably be our database's ID
      $movie->tmsid = $data->tmsId;
      //$movie->tmdbid = $tmdb->getTMDBId();
      //$movie->imdbid = $tmdb->getIMDBId();

      $movie->title = $data->title;
      $movie->description = is_null($tmdb->getOverview()) ? $data->description : $tmdb->getOverview();
      $movie->rating = $data->ratings[0]->code;
      $movie->poster = $tmdb->getPosterURL();
      $movie->backdrop = $tmdb->getBackdropURL();
      $movie->cast = $tmdb->getCast();

      // add genres; note that we try to use TMDB because unique IDs will become useful later (when doing recommendations)
      $movie->genres = array();
      if (!is_null($tmdb->getGenres())) {
        // add genre from TMDB
        $genres = $tmdb->getGenres();
      } else {
        // fallback to TMS
        $genres = $data->genres;
      }
      foreach ($genres as $genre) {
        // @todo if from TMS, search for existing genre in database and set id accordingly
        array_push($movie->genres, new Genre($genre));
      }

      // parse runtime "PT02H14M" --> "2 hr 14 min"
      preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
      $movie->runtime = '';
      $hours = intval($runtime[1]);
      if ($hours !== 0) {
        // don't include hours if they are 0
        $movie->runtime .= $hours.' hr ';
      }
      $movie->runtime .= intval($runtime[2]).' min';

      // @todo this is just randomly generating stuff at the moment
      $movie->mn_rating = rand(-1,1) * rand(1,10);

      // @todo all movies have the same events for now
      $movie->events = getEvents();

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

  // sort movies by rating
  usort($movies, array('Movie', 'cmp'));

  //print_r($movies);
  print json_encode($movies);

} else if ($request[0] == "friends") {

  print json_encode(getUsers());

} else if ($request[0] == "events") {

  print json_encode(getEvents());

}

function getUsers($guest = false) {
  $friends = array();
  $data = explode("\n", file_get_contents('mock/data/users.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    if ($guest) {
      $friend = new Guest(new User($line[0], $line[1]));
      $friend->user->photo = $line[2];
    } else {
      $friend = new User($line[0], $line[1]);
      $friend->photo = $line[2];
    }
    array_push($friends, $friend);
  }
  return $friends;
}

function getEvents() {

  $events = array();
  $friends = getUsers(true);
  $data = explode("\n", file_get_contents('mock/data/events.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    $event = new Event($line[0]);
    $event->movie = stripObject(new Movie($line[1]));
    $event->showtime = new Showtime($line[2]);
    $event->status = $line[3];
    $event->theater = new Theater(0, $line[5]);

    $friend_list = explode(",", $line[4]);
    foreach ($friend_list as $i) {
      array_push($event->guests, $friends[$i % sizeof($friends)]);
    }
    array_push($events, $event);
  }
  return $events;

}

// strips simple object of null properties
function stripObject($obj) {
  return (object) array_filter((array) $obj);
}