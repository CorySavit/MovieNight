<?php
header('Content-type: application/json');
require_once 'shared.php';

// setup database
$db = new medoo(DB_NAME);

$request = explode('/', $_GET['request']);
$request_type = $_SERVER['REQUEST_METHOD'];

if ($request[0] == "movies") {

  $date = '2014-04-15';

  if (sizeof($request) == 1) {
    switch ($request_type) {
      case 'GET':

        // get movies playing near current location
        $movies = $db->query("SELECT movies.id, movies.title, movies.poster
          FROM showtimes JOIN movies ON (movie_id = movies.id)
          WHERE DATE(time) = DATE(".$db->quote($date).") AND theater_id IN (
            SELECT id
            FROM theaters
            WHERE (
                3959 * acos(
                  cos(radians(".STATIC_LAT."))
                  * cos(radians(lat))
                  * cos(radians(lng) - radians(".STATIC_LNG."))
                  + sin(radians(".STATIC_LAT."))
                  * sin(radians(lat))
                )
            ) <= 31
          ) GROUP BY movie_id;")->fetchAll(PDO::FETCH_ASSOC);

        // @todo incorporate actual ratings
        foreach ($movies as &$movie) {
          $movie['mn_rating'] = rand(-1,1) * rand(1,10);
        }

        print json_encode($movies);

        break;
      default:
        echo INVALID_REQUEST;
    }
  } else {
    // assume second parameter is id

    // get movie information
    $movie = $db->get('movies', array(
      'id',
      'tmdb_id',
      'title',
      'description',
      'mpaa_rating',
      'poster',
      'runtime'
    ), array('id' => $request[1]));

    // get genres
    $movie['genres'] = $db->query("select g.name
      from movies2genres join genres as g on genre_id = g.id
      where movie_id = 1;")->fetchAll(PDO::FETCH_COLUMN);

    // get featured events
    // @todo add "and time > CURRENT_TIME"
    $movie['events'] = $db->query("select e.id, s.time, s.flag, t.name as theater_name
      from events as e
      join showtimes as s on showtime_id = s.id
      join theaters as t on s.theater_id = t.id
      where movie_id = ".$movie['id']." and public = 1
      order by s.time asc;")->fetchAll(PDO::FETCH_ASSOC);

    // add guest list
    get_guests($movie['events']);

    // get showtimes
    $theaters = $db->query("select s.id, time, flag, theater_id, name, address
      from showtimes as s join theaters as t on theater_id = t.id
      where movie_id = ".$movie['id']." and date(time) = date(".$db->quote($date).")
      order by time asc;")->fetchAll(PDO::FETCH_ASSOC);

    $movie['theaters'] = array();
    foreach ($theaters as $theater) {
      if (array_key_exists($theater['theater_id'], $movie['theaters'])) {
        array_push($movie['theaters'][$theater['theater_id']]['showtimes'], new Showtime($theater));
      } else {
        $movie['theaters'][$theater['theater_id']] = array(
          'id' => $theater['theater_id'],
          'name' => $theater['name'],
          'address' => $theater['address'],
          'showtimes' => array(new Showtime($theater))
        );
      }
    }
    $movie['theaters'] = array_values($movie['theaters']);

    // get cast from TMDB
    $tmdb = new TMDB();
    $cast = $tmdb->getCast($movie['tmdb_id']);
    $movie['cast'] = array();
    foreach ($cast as $member) {
      array_push($movie['cast'], array(
        'name' => $member['name'],
        'character' => $member['character'],
        'photo' => $tmdb->getProfileURL($member['profile_path'])
      ));
    }

    print json_encode($movie);

  }


  /*
  // parse runtime "PT02H14M" --> "2 hr 14 min"
  preg_match('/^PT(\d\d)H(\d\d)M$/', $data->runTime, $runtime);
  $movie->runtime = '';
  $hours = intval($runtime[1]);
  if ($hours !== 0) {
    // don't include hours if they are 0
    $movie->runtime .= $hours.' hr ';
  }
  $movie->runtime .= intval($runtime[2]).' min';
  */
  
  

} else if ($request[0] == "friends") {

  print json_encode(getUsers());

} else if ($request[0] == "events") {

  if (sizeof($request) == 1) {
    switch ($request_type) {
      case 'GET':

        if (array_key_exists('user_id', $_GET)) {
          // get all of user's events
          // @todo add "and time >= CURRENT_TIME" or maybe on a day level
          $events = $db->query("select e.id, m.title, s.time, s.flag, u2e.status
            from users2events as u2e
            join events as e on event_id = e.id
            join showtimes as s on e.showtime_id = s.id
            join movies as m on movie_id = m.id
            where user_id = ".$_GET['user_id'].";")->fetchAll(PDO::FETCH_ASSOC);

          get_guests($events);

          echo json_encode($events);

        } else {
          echo INVALID_REQUEST;
        }
        break;

      case 'POST':

        // create event
        $id = $db->insert('events', array(
          'showtime_id' => $_POST['showtime_id'],
          'admin_id' => $_POST['user_id']
        ));

        // attach user to event as admin
        $db->insert('users2events', array(
          'user_id' => $_POST['user_id'],
          'event_id' => $id,
          'status' => 2
        ));

        echo formatResponse(array(
          'id' => $id
        ));

        break;
      default:
        echo INVALID_REQUEST;
    }

  } else {
    // assume event id is passed in
    switch ($request_type) {
      case 'GET':

        if (array_key_exists('user_id', $_GET)) {
          $event = $db->query("select e.id, s.time, s.flag, t.id as theater_id, t.name as theater_name, t.address, e.admin_id, concat(u.first_name, ' ', u.last_name) as admin_name, u2e.status
            from events as e
            join showtimes as s on showtime_id = s.id
            join theaters as t on s.theater_id = t.id
            join users as u on e.admin_id = u.id
            left join users2events as u2e on u2e.user_id = ".$_GET['user_id']." and event_id = ".$request[1]."
            where e.id = ".$request[1].";")->fetchAll(PDO::FETCH_ASSOC);
          
          echo json_encode($event[0]);

        } else {
          echo INVALID_REQUEST;
        }

        break;

      case 'POST':

        // attach user to event
        $id = $db->insert('users2events', array(
          'user_id' => $_POST['user_id'],
          'event_id' => $request[1],
          'status' => $_POST['status']
        ));

        echo formatResponse(array(
          'id' => $id
        ));

        break;

      case 'PUT':

        // attach user to event
        parse_str(file_get_contents("php://input"), $_PUT);
        $db->update('users2events', array(
          'status' => $_PUT['status']
        ), array(
          'AND' => array(
            'user_id' => $_PUT['user_id'],
            'event_id' => $request[1]
          )
        ));

        echo formatResponse();

        break;

      default:
        echo INVALID_REQUEST;
    }
  }

} else if ($request[0] == "user") {

  if (sizeof($request) == 1) {
    switch ($request_type) {
      case 'POST':

        // create new user
        $hash = hashSSHA($_POST['password']);
        $date = date("Y-m-d H:i:s");
        $id = $db->insert('users', array(
          'email' => $_POST['email'],
          'first_name' => $_POST['first_name'],
          'last_name' => $_POST['last_name'],
          'password' => $hash["encrypted"],
          'salt' => $hash["salt"],
          'photo' => get_gravatar($_POST['email']),
          'created_at' => $date
        ));

        echo formatResponse(array(
          'id' => $id
        ));

        break;
      default:
        echo INVALID_REQUEST;
    }
  } else if ($request[1] == 'login') {
    switch ($request_type) {
      case 'POST':

        // login
        $data = $db->get('users', '*', array(
          'email' => $_POST['email']
        ));
        // @todo check to see if query had any errors
        // @todo this assumes password is sent via plaintext (obviously unsecure)
        $login_success = checkhashSSHA($data['salt'], $_POST['password']) == $data['password'] ? 1 : 0;
        if($login_success == 1){
          $data["login"] = $login_success;
          echo formatResponse($data);
        } else {
          echo formatResponse(array("login"=> $login_success));
        }

        break;
      default:
        echo INVALID_REQUEST;
    }
  } else  {
    // otherwise assume $request[1] is user id
    switch ($request_type) {
      case 'GET':

        // get user information
        $data = $db->get('users', array(
          'id',
          'email',
          'first_name',
          'last_name',
          'created_at'
        ), array('id' => $request[1]));
        echo formatResponse($data);

        break;
      case 'PUT':
        // @todo update user information
        echo INVALID_REQUEST;

        break;
      default:
        echo INVALID_REQUEST;
    }
  }

} else {
  echo INVALID_REQUEST;
}

function getUsers($guest = false) {
  $friends = array();
  $data = explode("\n", file_get_contents('mock/data/users.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    if ($guest) {
      $friend = new Guest(new User($line[0], $line[1]));
      $friend->user->photo = $line[2];
    } else {
      $friend = new User($line[0], $line[1]);
      $friend->photo = $line[2];
    }
    array_push($friends, $friend);
  }
  return $friends;
}

function getEvents() {

  $events = array();
  $friends = getUsers(true);
  $data = explode("\n", file_get_contents('mock/data/events.dat'));
  foreach ($data as $line) {
    $line = explode("\t", $line);
    $event = new Event($line[0]);
    $event->movie = stripObject(new Movie($line[1]));
    $event->showtime = new Showtime($line[2]);
    $event->status = $line[3];
    $event->theater = new Theater(0, $line[5]);

    $friend_list = explode(",", $line[4]);
    foreach ($friend_list as $i) {
      array_push($event->guests, $friends[$i % sizeof($friends)]);
    }
    array_push($events, $event);
  }
  return $events;

}

// strips simple object of null properties
function stripObject($obj) {
  return (object) array_filter((array) $obj);
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

// format a JSON response
function formatResponse($array = array()) {
  global $db;
  $error = $db->error();
  $array['success'] = is_null($error[2]) ? 1 : 0;
  $array['error'] = $error[2];
  return json_encode($array);
}
