# MovieNight API

The MovieNight API provides the necessary endpoints for the MovieNight Android Application. Currently, the API utilizes the [TMS Data Delivery API](http://developer.tmsapi.com/TMS_Data_Delivery_APIs) for relevant movie information. The API can be accessed at [labs.amoscato.com/movienight-api](http://labs.amoscato.com/movienight-api/).

In order to run the API locally, duplicate `auth.example.php`, rename the file to `auth.php` and add the appropriate API keys.

## Methods

Below are a list of methods supported by the API. The response for all methods include `success: {0,1}` and `error: {<msg>,null}`.

### /events

* **GET** — returns set of mock data at the moment

### /friends

* **GET** — returns set of mock data at the moment

### /movies

* **GET** — returns a list of all movies currently playing in a predefined location

	**Response** {`id`, `title`, `poster`, `mn_rating`}

### /movies/{id}

* **GET** — returns information for a specific movie


### /user

* **POST** — create new user

	**Request** {`first_name`, `last_name`, `email`, `password`}
	
	**Response** {`id`}

### /user/{id}

* **GET** — logs user in
	
	**Response** {`id`, `email`, `first_name`, `last_name`, `created_at`}

### /user/login

* **POST** — logs user in

	**Request** {`email`, `password`}
	
	**Response** {`login: {0,1}`}