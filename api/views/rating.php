<?php
require_once '../shared.php';

// setup database
$db = new medoo(DB_NAME);

if (!array_key_exists('movie_id', $_GET)) {
  echo "Must pass in movie_id";
  exit(1);
}

$rating = $db->query("select mn_rating as mn, mn_rating_count as mn_count
  from movies
  left join (
    select movie_id, sum(rating) as mn_rating, count(movie_id) as mn_rating_count
    from ratings
    group by movie_id
  ) as r on movie_id = id
  where id = ".$_GET['movie_id'].";")->fetch(PDO::FETCH_ASSOC);

$percent = 0;
if ($rating['mn_count'] != 0) {
  $percent = $rating['mn'] / $rating['mn_count'];
}
?><!doctype html>
<html>
  <head>
    <title>MovieNight Rating</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
  </head>
  <body>
    <div id="meter">
      <div class="circle"></div>
      <div id="red" class="circle red"></div>
      <div id="fill" class="circle fill"></div>
      <div class="metric">
        <p id="percent" class="percent">0%</p>
        <p class="label">from <?php echo ($rating['mn_count'] ? $rating['mn_count'] : '0').($rating['mn_count'] == 1 ? ' friend' : ' friends'); ?></p>
      </div>
    </div>
  </body>

  <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
  <script>window.jQuery || document.write('<script src="js/lib/jquery.js"><\/script>')</script>
  <script>
    var percent = <?php echo $percent; ?>;
    var rotate = <?php echo -1 * (1 - $percent) * 180; ?>;
    var $fill = $('#fill');
    var $percent = $('#percent');
    var $redFill = $('#red');

    function fillMeter() {
      if (percent > 0) {
        $fill.animate({
          'text-indent': rotate
        }, {
          duration: 1500,
          step: function(now) {
            $fill.css('-webkit-transform', 'rotate(' + now + 'deg)');
            $percent.text(parseInt(((now + 180) / (rotate + 180)) * percent * 100) + '%');
          }
        });

        $redFill.fadeIn(1500);
      }
    }

  </script>
</html>