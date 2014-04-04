package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Movie implements Serializable {
	private static final long serialVersionUID = 1L;

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
	static final String MN_RATING = "mn_rating";
	static final String EVENTS = "events";

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
	int mnRating;
	List<Event> events;

	public Movie(JSONObject data) {

		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getString(data, ID);
		tmsid = JSON.getString(data, TMS_ID);
		title = JSON.getString(data, TITLE);
		description = JSON.getString(data, DESCRIPTION);
		poster = JSON.getString(data, POSTER);
		rating = JSON.getString(data, RATING);
		runtime = JSON.getString(data, RUNTIME);
		mnRating = JSON.getInt(data, MN_RATING);

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
		
		events = new ArrayList<Event>();
		JSONArray myEvents = JSON.getJSONArray(data, EVENTS);
		if (myEvents != null) {
			for (int i = 0; i < myEvents.length(); i++) {
				events.add(new Event(JSON.getJSONObject(myEvents, i)));
			}
		}
		
	}
}
