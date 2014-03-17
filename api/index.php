<?php
require_once 'auth.php';
require_once 'tmdbposter.php';

class Movie {
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
  public $showtimes;

  public function __construct($name) {
    if (!is_null($name)) {
      $this->name = $name;
    }
    $this->showtimes = array();
  }
}

$request = explode('/', $_GET['request']);

if ($request[0] == "movies") {
  // default zip code
  $zip = 15213;

  print file_get_contents('mock/movies-showings');
  exit(1);

  //$result = file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . ONCONNECT_KEY);
  $result = json_decode(file_get_contents('mock/movies-showings'));

  $movies = array();
  foreach ($result as $data) {
    $movie = new Movie();
    $movie->title = $data->title;
    $movie->description = $data->longDescription;
    $movie->rating = $data->ratings[0]->code;
    $movie->genres = $data->genres;

    // parse runtime "PT02H14M" --> "2 hr 14 min"
    preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
    $movie->runtime = intval($runtime[1]).' hr '.intval($runtime[2]).' min';

    // group showtimes by theater
    foreach ($data->showtimes as $showtime) {
      $id = $showtime->theatre->id;

      // add new theater if it does not already exist
      if (!array_key_exists($id, $movie->theaters)) {
        $movie->theaters[$id] = new Theater($showtime->theatre->name);
      }

      // add showtime to appropriate theater object
      // "2014-03-14T13:10" --> "2014-03-14 13:10"
      array_push($movie->theaters[$id]->showtimes, str_replace('T', ' ', $showtime->dateTime));
    }

    // add poster via the movie database
    $tmdb = new TMDBposter();
    $tmdb_results = $tmdb->searchMovie($movie->title,'en');
    $movie->poster = 'http://image.tmdb.org/t/p/w342'.$tmdb_results['results'][0]['poster_path'];
    
    array_push($movies, $movie);
  }

  //print_r($movies);
  print json_encode($movies);
}
