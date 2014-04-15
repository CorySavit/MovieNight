<?php
require_once '../shared.php';

// setup database
$db = new medoo(DB_NAME);

// make call to google places api
$result = json_decode(file_get_contents('https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='.STATIC_LAT.','.STATIC_LNG.'&radius='.RADIUS_METERS.'&types=movie_theater&sensor=false&key='.PLACES_KEY));

foreach ($result->results as $theater) {

  $id = $db->get('theaters', 'id', array('google_id' => $theater->id));

  if (!$id) {
    // insert theater into database if it doesn't already exist
    myLog('inserting "'.$theater->name.'" into database');
    $db->insert('theaters', array(
      'name' => $theater->name,
      'google_id' => $theater->id,
      'address' => $theater->vicinity,
      'lat' => $theater->geometry->location->lat,
      'lng' => $theater->geometry->location->lng
    ));
  }

}

function myLog($output) {
  if (DEBUG) {
    echo $output."\n";
  }
}
