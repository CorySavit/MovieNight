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

define("FLAG_3D", 1);
define("FLAG_IMAX", 2);

class Showtime {
  public $time;
  public $flag;

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

define("STATUS_INVITED", 0);
define("STATUS_ACCEPTED", 1);
define("STATUS_DECLINED", -1);

class Guest {
  public $user;
  public $status;

  public function __construct($user) {
    if (!is_null($user)) {
      $this->user = $user;
    }
    $this->status = STATUS_INVITED;
  }
}
