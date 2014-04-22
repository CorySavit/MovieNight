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
      case 'GET': # /movies

        // get movies playing near current location
        $movies = $db->query("select m.id, m.title, m.poster, m.mpaa_rating, m.runtime, mn_rating
          from showtimes as s
          join movies as m on (s.movie_id = m.id)
          left join (
            select movie_id, sum(rating) as mn_rating
            from ratings
            group by movie_id
          ) as r on r.movie_id = s.movie_id
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
          ) GROUP BY s.movie_id;")->fetchAll(PDO::FETCH_ASSOC);

        // @todo incorporate actual ratings
        foreach ($movies as &$movie) {
          //$movie['mn_rating'] = rand(-1,1) * rand(1,10);
          $movie['genres'] = get_genres($movie['id']);
        }

        print json_encode($movies);

        break;
      default:
        echo INVALID_REQUEST;
    }

  } else if (sizeof($request) == 2 && $request_type == 'GET') { # /movies/{id}
    // assume second parameter is id

    // get movie information
    $movie = $db->query("select id, tmdb_id, rotten_id, title, description, mpaa_rating, poster, runtime, mn_rating, mn_rating_count
      from movies
      left join (
        select movie_id, sum(rating) as mn_rating, count(movie_id) as mn_rating_count
        from ratings
        group by movie_id
      ) as r on movie_id = id
      where id = ".$request[1].";")->fetch(PDO::FETCH_ASSOC);

    // get genres
    $movie['genres'] = get_genres($request[1]);

    // get featured events
    // @todo add "and time > CURRENT_TIME"
    $movie['events'] = $db->query("select e.id, s.time, s.flag, t.name as theater_name
      from events as e
      join showtimes as s on showtime_id = s.id
      join theaters as t on s.theater_id = t.id
      where movie_id = ".$movie['id']." and public = 1
      order by s.time asc;")->fetchAll(PDO::FETCH_ASSOC);

    // add guests who are attending
    get_guests($movie['events'], true);

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
    $tmdb = new TMDB($movie['tmdb_id']);
    $cast = $tmdb->getCast();
    $movie['cast'] = array();
    foreach ($cast as $member) {
      array_push($movie['cast'], array(
        'name' => $member['name'],
        'character' => $member['character'],
        'photo' => $tmdb->getProfileURL($member['profile_path'])
      ));
    }

    // get rotten tomatoes rating
    $rotten = new RottenTomatoes($movie['rotten_id']);
    $movie['rotten_rating'] = array(
      'critics' => $rotten->getCriticsRating(),
      'audience' => $rotten->getAudienceRating()
    );

    // get TMDB rating
    $movie['tmdb_rating'] = $tmdb->getRating();

    print json_encode($movie);

  } else if ($request[2] == 'rating') {

    if (array_key_exists('user_id', $_POST) && array_key_exists('rating', $_POST)) {

      $id = $db->insert('ratings', array(
        'user_id' => $_POST['user_id'],
        'movie_id' => $request[1],
        'rating' => $_POST['rating']
      ));

      echo formatResponse(array(
        'id' => $id
      ));

    } else {
      echo INVALID_REQUEST;
    }

  } else {
    echo INVALID_REQUEST;
  }
  
} else if ($request[0] == "friends") {

  if (array_key_exists('user_id', $_GET)) {

    echo json_encode($db->query("select friend_id as id, concat(u.first_name, ' ', u.last_name) as name, u.photo
      from (
        select friend_id
        from friends
        where user_id = ".$_GET['user_id']."
        union
        select user_id
        from friends
        where friend_id = ".$_GET['user_id']."
      ) as f
      join users as u on f.friend_id = u.id
      order by u.first_name;")->fetchAll(PDO::FETCH_ASSOC));

  } else {
    echo INVALID_REQUEST;
  }

} else if ($request[0] == "events") {

  if (sizeof($request) == 1) {
    switch ($request_type) {
      case 'GET': # /events

        if (array_key_exists('user_id', $_GET)) {
          // get all of user's events
          // @todo add "and time >= CURRENT_TIME" or maybe on a day level
          $events = $db->query("select e.id, m.title, s.time, s.flag, u2e.status
            from users2events as u2e
            join events as e on event_id = e.id
            join showtimes as s on e.showtime_id = s.id
            join movies as m on movie_id = m.id
            where user_id = ".$_GET['user_id']."
            order by s.time asc;")->fetchAll(PDO::FETCH_ASSOC);

          // get guests who are attending
          get_guests($events, true);

          echo json_encode($events);

        } else {
          echo INVALID_REQUEST;
        }
        break;

      case 'POST': # /events

        // create event
        $id = $db->insert('events', array(
          'showtime_id' => $_POST['showtime_id'],
          'admin_id' => $_POST['user_id']
        ));

        // attach user to event as admin
        $db->insert('users2events', array(
          'user_id' => $_POST['user_id'],
          'event_id' => $id,
          'status' => STATUS_ADMIN
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
      case 'GET': # /events/{id}

        if (array_key_exists('user_id', $_GET)) {
          $event = $db->query("select e.id, s.time, s.flag, s.movie_id, t.id as theater_id, t.name as theater_name, t.address, e.admin_id, concat(u.first_name, ' ', u.last_name) as admin_name, u2e.status
            from events as e
            join showtimes as s on showtime_id = s.id
            join theaters as t on s.theater_id = t.id
            join users as u on e.admin_id = u.id
            left join users2events as u2e on u2e.user_id = ".$_GET['user_id']." and event_id = ".$request[1]."
            where e.id = ".$request[1].";")->fetch(PDO::FETCH_ASSOC);

          // get movie information (for header)
          $event['movie'] = $db->get('movies', array(
            'title',
            'mpaa_rating',
            'runtime',
            'poster'
          ), array('id' => $event['movie_id']));
          $event['movie']['genres'] = get_genres($event['movie_id']);

          // get all guests
          $guests = get_guests($event);
          $group = array(
            STATUS_ACCEPTED => array(),
            STATUS_INVITED => array(),
            STATUS_DECLINED => array(),
          );
          foreach ($guests as $guest) {
            $index = $guest['status'];
            if ($guest['status'] == STATUS_ADMIN) {
              $index = STATUS_ACCEPTED;
            }
            array_push($group[$index], $guest);
          }
          $event['guests'] = $group;
          
          echo json_encode($event);

        } else {
          echo INVALID_REQUEST;
        }

        break;

      case 'POST': # /events/{id}

        // attach user to event
        $id = $db->insert('users2events', array(
          'user_id' => $_POST['user_id'],
          'event_id' => $request[1],
          'status' => (array_key_exists('status', $_POST) ? $_POST['status'] : 0)
        ));

        echo formatResponse(array(
          'id' => $id
        ));

        break;

      case 'PUT': # /events/{id}

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
      case 'POST': # /user

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
      case 'POST': # /user/login

        // login
        $data = $db->get('users', '*', array(
          'email' => $_POST['email']
        ));

        // @todo this assumes password is sent via plaintext (obviously unsecure)
        $login_success = checkhashSSHA($data['salt'], $_POST['password']) == $data['password'] ? 1 : 0;

        // don't return password/salt on API response
        unset($data['password']);
        unset($data['salt']);

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
      case 'GET': # /user/{id}

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
