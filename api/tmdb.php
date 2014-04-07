<?php
require_once 'auth.php';

class TMDB {
	const _API_URL_ = "http://api.themoviedb.org/3/";
	private $_apikey;
	private $_lang;
	private $_config;
	private $_movie;

		public function  __construct($query, $year) {
			$this->setApikey(THEMOVIEDB_KEY);
			$this->setLang();
			
			$this->_config = $this->_call("configuration");
			if (empty($this->_config)) {
				echo "Unable to read configuration, verify that the API key is valid";
				exit;
			}

			// search for movie based on query string and year
			$result = $this->_call("search/movie", "query=".urlencode($query));
			if (sizeof($result['results'])) {
				$this->_movie = $this->_call("movie/".$result['results'][0]['id']);
			} else {
				//echo "Unable to find movie.";
				$this->_movie = null;
			}
		}

		private function setApikey($apikey) {
			$this->_apikey = (string) $apikey;
		}

		private function getApikey() {
			return $this->_apikey;
		}

		private function setLang($lang="en") {
			$this->_lang = $lang;
		}

		private function getLang() {
			return $this->_lang;
		}

		// API METHODS

		private function isMovieSet() {
			return isset($this->_movie);
		}

		private function getMovie($prop) {
			if ($this->isMovieSet() && array_key_exists($prop, $this->_movie)) {
				return $this->_movie[$prop];
			}
			return null;
		}

		private function getImagePath($size="w342") {
			return $this->_config['images']['base_url'].$size;
		}

		public function getPosterURL($size="w342") {
			return ($this->getMovie('poster_path') == "") ? null : $this->getImagePath($size).$this->getMovie('poster_path');
		}

		public function getBackdropURL($size="w300") {
			return ($this->getMovie('backdrop_path') == "") ? null : $this->getImagePath($size).$this->getMovie('backdrop_path');
		}

		public function getGenres() {
			return $this->getMovie('genres');
		}

		public function getIMDBID() {
			return $this->getMovie('imdb_id');
		}

		public function getOverview() {
			return $this->getMovie('overview');
		}

		public function getTagline() {
			return $this->getMovie('tagline');
		}

		private function setCredits() {
			if ($this->isMovieSet()) {
				$result = $this->_call("movie/".$this->getMovie('id')."/credits");
				$this->_movie['cast'] = $result['cast'];
				$this->_movie['crew'] = $result['crew'];
			}
		}

		public function getCast() {
			$key = 'cast';
			if ($this->isMovieSet() && !array_key_exists($key, $this->_movie)) {
				$this->setCredits();
			}
			return $this->getMovie($key);
		}

		public function getCrew() {
			$key = 'crew';
			if ($this->isMovieSet() && !array_key_exists($key, $this->_movie)) {
				$this->setCredits();
			}
			return $this->getMovie($key);
		}

		public function getKeywords() {
			$key = 'keywords';
			if (!property_exists($this->_movie, $key)) {
				$result = $this->_call("movie/".$this->getMovie('id')."/keywords");
				$this->_movie[$key] = $result['keywords'];
			}
			return $this->_movie[$key];
		}

		public function getVideos() {
			$key = 'videos';
			if (!property_exists($this->_movie, $key)) {
				$result = $this->_call("movie/".$this->getMovie('id')."/videos");
				$this->_movie[$key] = $result['results'];
			}
			return $this->getMovie($key);
		}

		// call the movie db for info
		private function _call($action, $text="", $lang="") {
			$lang = empty($lang) ? $this->getLang() : $lang;
			$url = TMDB::_API_URL_.$action."?api_key=".$this->getApikey()."&language=".$lang."&".$text;
			$ch = curl_init();
				curl_setopt($ch, CURLOPT_URL, $url);
				curl_setopt($ch, CURLOPT_HEADER, 0);
				curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
				curl_setopt($ch, CURLOPT_FAILONERROR, 1);

			$results = curl_exec($ch);
			$headers = curl_getinfo($ch);

			$error_number = curl_errno($ch);
			$error_message = curl_error($ch);

			curl_close($ch);
			$results = json_decode(($results),true);
			return (array) $results;
		}

}
