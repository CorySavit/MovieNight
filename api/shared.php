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
