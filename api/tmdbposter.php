<?php

class TMDBposter{
	const _API_URL_ = "http://api.themoviedb.org/3/";
	private $_apikey;
	private $_lang;
	private $_imgUrl;

		public function  __construct() {
			$this->setApikey('#######APIKEY#######');
			$this->setLang('en');
			$conf = $this->getConfig();
			if (empty($conf)){echo "Unable to read configuration, verify that the API key is valid";exit;}
			$this->setImageURL($conf);
		}

		private function setApikey($apikey) {
			$this->_apikey = (string) $apikey;
		}

		private function getApikey() {
			return $this->_apikey;
		}

		public function setLang($lang="en") {
			$this->_lang = $lang;
		}

		public function getLang() {
			return $this->_lang;
		}

		public function setImageURL($config) {
			$this->_imgUrl = (string) $config['images']["base_url"];
		}

		public function getImageURL($size="original") {
			return $this->_imgUrl . $size;
		}

		public function moviePoster($idMovie)
		{
			$posters = $this->movieInfo($idMovie,"images",false);
			$posters =$posters['posters'];
			return $posters;
		}

		public function movieCast($idMovie)
		{
			$castingTmp = $this->movieInfo($idMovie,"casts",false);
			foreach ($castingTmp['cast'] as $castArr){
				$casting[]=$castArr['name']." - ".$castArr['character'];
			}
			return $casting;
		}//end of movieCast

		public function movieInfo($idMovie,$option="",$print=false){
			$option = (empty($option))?"":"/" . $option;
			$params = "movie/" . $idMovie . $option;
			$movie= $this->_call($params,"");
				return $movie;
		}

		public function searchMovie($movieTitle){
			$movieTitle="query=".urlencode($movieTitle);
			return $this->_call("search/movie",$movieTitle,$this->_lang);
		}

		public function getConfig() {
			return $this->_call("configuration","");
		}

		/*call the movie db for info*/
		private function _call($action,$text,$lang=""){
			$lang=(empty($lang))?$this->getLang():$lang;
			$url= TMDBposter::_API_URL_.$action."?api_key=".$this->getApikey()."&language=".$lang."&".$text;
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
?>
