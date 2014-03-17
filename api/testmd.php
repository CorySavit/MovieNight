<!DOCTYPE html>
<html>
  <head>
    <title>POSTER</title>
</head>
<?
include("tmdbposter.php");
$movietitle = $_GET['movie'];
//create a new "object"
$tmdbposter = new TMDBposter();
//search for movie based on title
$movies = $tmdbposter->searchMovie($movietitle,'en');

$movieid = $movies['results'][0]['id'];

$posters = $tmdbposter->moviePoster($movieid);
$imgfile = $posters[0]['file_path'];
$imgurl = $tmdbposter->getImageURL();
echo "<img src='".$imgurl.$imgfile."'>";
