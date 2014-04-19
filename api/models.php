<?php

class Movie {
  public $id;
  public $tmsid;
  public $tmdbid;
  public $imdbid;

  public $title;
  public $description;
  public $genres;
  public $rating;
  public $runtime;
  public $poster;
  public $backdrop;
  public $cast;
  public $mn_rating;

  // @todo could move to separate endpoint
  public $theaters;
  public $events;

  public function __construct($title = null) {
    if (!is_null($title)) {
      $this->title = $title;
    }
    $this->theaters = array();
    $this->events = array();
  }

  public function cmp($a, $b) {
    if ($a->mn_rating == $b->mn_rating) {
      return 0;
    }
    return ($a->mn_rating < $b->mn_rating) ? 1 : -1;
  }
}

class Theater {
  public $id;
  public $name;
  public $ticketurl; // @todo move this to showtime
  public $showtimes;

  public function __construct($id = null, $name = null) {
    if (!is_null($id) && !is_null($name)) {
      $this->id = $id;
      $this->name = $name;
    }
    $this->showtimes = array();
  }
}

define("FLAG_3D", 1);
define("FLAG_IMAX", 2);

class Showtime {
  public $id;
  public $time;
  public $flag;

  public function __construct($array) {
    foreach($array as $key => $value) {
      if (property_exists($this, $key)) {
        $this->$key = $value;
      }
    }
  }
}

define("STATUS_ADMIN", 1);
define("STATUS_ACCEPTED", 2);
define("STATUS_INVITED", 3);
define("STATUS_DECLINED", 4);

class Event {
  public $id;
  public $movie;
  public $showtime;
  public $theater;
  public $admin;
  public $status;
  public $guests;

  public function __construct($id = null) {
    if (!is_null($id)) {
      $this->id = $id;
    }
    $this->guests = array();
  }
}

class User {
  public $id;
  public $name;
  public $email;
  public $photo;

  public function __construct($id = null, $name = null) {
    if (!is_null($id) && !is_null($name)) {
      $this->id = $id;
      $this->name = $name;
    }
  }
}

class Guest {
  public $user;
  public $status;

  public function __construct($user = null) {
    if (!is_null($user)) {
      $this->user = $user;
    }
    $this->status = STATUS_INVITED;
  }
}

class Genre {
  public $id;
  public $name;

  public function __construct($param = null) {
    if (gettype($param) === "array") {
      $this->id = $param['id'];
      $this->name = $param['name'];
    } else {
      // @todo look search for name in database and set existing id
      $this->id = 1;
      $this->name = $param;
    }
  }
}
