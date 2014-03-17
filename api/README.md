# MovieNight API

The MovieNight API provides the necessary endpoints for the MovieNight Android Application. Currently, the API utilizes the [TMS Data Delivery API](http://developer.tmsapi.com/TMS_Data_Delivery_APIs) for relevant movie information. The API can be accessed at [labs.amoscato.com/movienight-api](http://labs.amoscato.com/movienight-api/).

In order to run the API locally, duplicate `auth.example.php`, rename the file to `auth.php` and add the appropriate API keys.

## Methods

Below are a list of methods supported by the API.

### /movies

Returns a list of all movies currently playing in local theatres, with showtimes.
