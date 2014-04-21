<?php
require_once 'auth.php';
require_once 'tmdb.php';
require_once 'rottentomatoes.php';
require_once 'models.php';
require_once 'medoo.php';

// prints out some verbose output
define("DEBUG", true);
define("INVALID_REQUEST", "Invalid Request");

// @todo eventually these won't be hardcoded
define("STATIC_LAT", '40.462712');
define("STATIC_LNG", '-79.965347');
define("RADIUS_METERS", 50000);
define("RADIUS_MILES", 31);

/**
 * Get either a Gravatar URL or complete image tag for a specified email address.
 *
 * @param string $email The email address
 * @param string $s Size in pixels, defaults to 80px [ 1 - 2048 ]
 * @param string $d Default imageset to use [ 404 | mm | identicon | monsterid | wavatar ]
 * @param string $r Maximum rating (inclusive) [ g | pg | r | x ]
 * @param boole $img True to return a complete IMG tag False for just the URL
 * @param array $atts Optional, additional key/value attributes to include in the IMG tag
 * @return String containing either just a URL or a complete image tag
 * @source http://gravatar.com/site/implement/images/php/
 */
function get_gravatar( $email, $s = 80, $d = 'mm', $r = 'g', $img = false, $atts = array() ) {
  $url = 'http://www.gravatar.com/avatar/';
  $url .= md5( strtolower( trim( $email ) ) );
  $url .= "?s=$s&d=$d&r=$r";
  if ( $img ) {
      $url = '<img src="' . $url . '"';
      foreach ( $atts as $key => $val )
          $url .= ' ' . $key . '="' . $val . '"';
      $url .= ' />';
  }
  return $url;
}

// gets guest list for event
// @param attending determines whether or not only display people who are attending
function get_guests(&$event, $attending = false) {
  global $db;

  $attend = $attending ? ' and (u2e.status = 1 or u2e.status = 2)' : '';

  if (!array_key_exists('id', $event)) {
    // we are dealing wtih an array of events
    foreach ($event as &$item) {
      $item['guests'] = get_guests($item, $attending);
    }
  } else {
    // we are just dealing with one event
    $result = $db->query("select u.id, concat(u.first_name, ' ', u.last_name) as name, u.photo, u2e.status
      from users2events as u2e
      join users as u on user_id = u.id
      where event_id = ".$event['id'].$attend."
      order by u2e.timestamp desc;")->fetchAll(PDO::FETCH_ASSOC);

    return $result;
  }
}

function get_genres($id) {
  global $db;
  
  return $db->query("select g.name
    from movies2genres join genres as g on genre_id = g.id
    where movie_id = ".$id.";")->fetchAll(PDO::FETCH_COLUMN);
}

// format a JSON response
function formatResponse($array = array()) {
  global $db;
  $error = $db->error();
  $array['success'] = is_null($error[2]) ? 1 : 0;
  $array['error'] = $error[2];
  return json_encode($array);
}

// encrypt password
function hashSSHA($password) {
  $salt = sha1(rand());
  $salt = substr($salt, 0, 10);
  $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
  $hash = array("salt" => $salt, "encrypted" => $encrypted);
  return $hash;
}

function checkhashSSHA($salt, $password) {
  $hash = base64_encode(sha1($password . $salt, true) . $salt);
  return $hash;
}
