<?php
require_once 'auth.php';
require_once 'tmdbposter.php';

class Movie {
  public $id;
  public $tmsid;
  public $title;
  public $description;
  public $genres;
  public $poster;
  public $rating;
  public $runtime;
  public $theaters;

  public function __construct() {
    $this->theaters = array();
  }
}

class Theater {
  public $name;
  public $ticketurl;
  public $showtimes;

  public function __construct($name) {
    if (!is_null($name)) {
      $this->name = $name;
    }
    $this->showtimes = array();
  }
}

class Showtime {
  public $time;
  public $flag; // 0 = normal; 1 = 3D; 2 = IMAX

  public function __construct($time) {
    if (!is_null($time)) {
      $this->time = $time;
    }
    $flag = 0;
  }
}

$request = explode('/', $_GET['request']);

if ($request[0] == "movies") {
  // default zip code
  $zip = 15213;

  print file_get_contents('mock/movies');
  exit(1);

  $result = json_decode(file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . ONCONNECT_KEY));
  //print_r($result);

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
      $movie->runtime = intval($runtime[1]).' hr '.intval($runtime[2]).' min';

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
        $theater = new Theater($showtime->theatre->name);
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
          $time->flag = 1;
        } else if (strpos($showtime->quals, 'IMAX')) {
          $time->flag = 2;
        }
      }

      // add showtime to appropriate theater object
      // @todo make sure these are sorted chronologically
      array_push($movie->theaters[$id]->showtimes, $time);
    }
    
    $movies[$data->rootId] = $movie;
  }

  //print_r($movies);
  print json_encode($movies);
}
