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

  public function __construct($title = null) {
    if (!is_null($title)) {
      $this->title = $title;
    }
    $this->theaters = array();
  }
}

class Theater {
  public $id;
  public $name;
  public $ticketurl;
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
  public $time;
  public $flag;

  public function __construct($time = null) {
    if (!is_null($time)) {
      $this->time = $time;
    }
    $flag = 0;
  }
}

define("STATUS_INVITED", 0);
define("STATUS_ACCEPTED", 1);
define("STATUS_DECLINED", -1);
define("STATUS_ADMIN", 2);

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
