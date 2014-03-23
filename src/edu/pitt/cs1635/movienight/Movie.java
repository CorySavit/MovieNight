package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Movie implements Serializable {

	// JSON keys
	static final String ID = "id";
	static final String TMS_ID = "tmsid";
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String GENRES = "genres";
	static final String POSTER = "poster";
	static final String RATING = "rating";
	static final String RUNTIME = "runtime";
	static final String THEATERS = "theaters";

	// object variables
	String id; // @todo these ids should probably be ints
	String tmsid;
	String title;
	String description;
	List<String> genres;
	String poster;
	String rating;
	String runtime;
	List<Theater> theaters;

	public Movie(JSONObject data) {

		id = JSON.getString(data, ID);
		tmsid = JSON.getString(data, TMS_ID);
		title = JSON.getString(data, TITLE);
		description = JSON.getString(data, DESCRIPTION);
		poster = JSON.getString(data, POSTER);
		rating = JSON.getString(data, RATING);
		runtime = JSON.getString(data, RUNTIME);

		genres = new ArrayList<String>();
		JSONArray myGenres = JSON.getJSONArray(data, GENRES);
		if (myGenres != null) {
			for (int i = 0; i < myGenres.length(); i++) {
				genres.add(JSON.getString(myGenres, i));
			}
		}

		theaters = new ArrayList<Theater>();
		JSONObject myTheaters = JSON.getJSONObject(data, THEATERS);
		if (myTheaters != null) {
			Iterator<?> keys = myTheaters.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				theaters.add(new Theater(JSON.getJSONObject(myTheaters, key)));
			}
		}
		
	}
}