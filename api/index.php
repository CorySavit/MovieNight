<?php
require 'auth.php';

$request = explode('/', $_GET['request']);

if ($request[0] == "movies") {
  // default zip code
  $zip = 15213;

  //$result = file_get_contents('http://data.tmsapi.com/v1/movies/showings?startDate=' . date("Y-m-d") . '&zip=' . $zip . '&api_key=' . $ONCONNECT_KEY);
  $result = file_get_contents('mock/movies-showings');

  //print_r(json_decode($result));
  print $result;
}
