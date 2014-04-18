<?php
require_once 'auth.php';
require_once 'tmdb.php';
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
function get_guests(&$event) {
  global $db;

  if (is_array($event)) {
    foreach ($event as &$item) {
      $item['guests'] = get_guests($item['id']);
    }
  } else {
    return $db->query("select u.id, u.photo
      from users2events
      join users as u on user_id = u.id
      where event_id = ".$event['id'].";")->fetchAll(PDO::FETCH_ASSOC);
  }
}
