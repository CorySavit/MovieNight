<?php
require_once 'auth.php';

class RottenTomatoes {
  const BASE_URL = "http://api.rottentomatoes.com/api/public/v1.0/";
  private $movie;

  public function __construct($query = null, $params = null) {

    if (!is_null($params)) {
      // we need to search for movie
      $result = $this->_call('movies', array(
        'q' => $query,
        'page_limit' => 5
      ));

      foreach ($result->movies as $movie) {
        if (property_exists($movie, 'alternate_ids') && property_exists($movie->alternate_ids, 'imdb')) {
          // if rotten tomatoes returns imdb id
          if ($movie->alternate_ids->imdb == $params['imdb_id']) {
            $this->movie = $movie;
            break;
          }
        } else if ($movie->year == $params['year']) {
          // otherwise, just compare release year
          $this->movie = $movie;
          break;
        }
      }

    } else if (!is_null($query)) {
      // we already have rotten tomatoes ID
      $this->movie = $this->_call('movies/'.$query);
    }
  }

  private function _call($method, $params = array()) {
    $params['apikey'] = ROTTEN_KEY;
    return json_decode(file_get_contents(RottenTomatoes::BASE_URL.$method.'.json?'.http_build_query($params)));
  }

  private function _isSet() {
    return isset($this->movie);
  }

  private function _get($prop) {
    if ($this->_isSet() && property_exists($this->movie, $prop)) {
      return $this->movie->{$prop};
    }
    return null;
  }

  public function getID() {
    return $this->_get('id');
  }

  public function getCriticsRating() {
    return $this->_get('ratings')->critics_score;
  }

  public function getAudienceRating() {
    return $this->_get('ratings')->audience_score;
  }
}