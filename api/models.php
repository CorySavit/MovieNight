<?php

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
  public $id;
  public $name;
  public $ticketurl;
  public $showtimes;

  public function __construct($id, $name) {
    if (!is_null($id) && !is_null($name)) {
      $this->id = $id;
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

class Event {
  public $id;
  public $movie;
  public $showtime;
  public $theater;
  public $admin;
}

class User {
  public $id;
  public $name;
  public $email;
  public $photo;

  public function __construct($id, $name) {
    if (!is_null($id) && !is_null($name)) {
      $this->id = $id;
      $this->name = $name;
    }
  }
}
