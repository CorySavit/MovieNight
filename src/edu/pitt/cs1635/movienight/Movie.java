package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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
		try {
			id = data.getString(ID);
			tmsid = data.getString(TMS_ID);
			title = data.getString(TITLE);
			description = data.getString(DESCRIPTION);
			poster = data.getString(POSTER);
			rating = data.getString(RATING);
			runtime = data.getString(RUNTIME);
			
			genres = new ArrayList<String>();
			JSONArray myGenres = data.getJSONArray(GENRES);
			for (int i = 0; i < myGenres.length(); i++) {
				genres.add(myGenres.getString(i));
			}
			
			theaters = new ArrayList<Theater>();
			JSONObject myTheaters = data.getJSONObject(THEATERS);
			Iterator<?> keys = myTheaters.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (myTheaters.get(key) instanceof JSONObject) {
					theaters.add(new Theater(myTheaters.getJSONObject(key)));
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// @todo should probably add get methods
	
}
