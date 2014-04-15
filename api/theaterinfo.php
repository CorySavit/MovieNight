<?php
header('Content-type: application/json');
require_once 'auth.php';

// setup database
require_once 'medoo.php';
$db = new medoo(DB_NAME);
define("INVALID_REQUEST", "Invalid Request");

$lat = '40.462712'
$lng =	'-79.965347'
$radius = '50000'

$result = json_decode(file_get_contents('https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='+$lat+','+$lng+'&radius='+$radius+'&types=movie_theater&sensor=false&key='+PLACES_KEY)


foreach($result as $theater){
	$theater_name = $theater->name;
	$theater_address = $theater->vicinity;
	$theater_lat = $theater->geometry->lat
	$theater_lng = $theater->geometry->lng

	$db->insert('theaters', array(
		'tms_id' =>	,
		'name' =>	,
		'address' =>	,
		'lat' =>		,
		'lng' =>	,
		))
}