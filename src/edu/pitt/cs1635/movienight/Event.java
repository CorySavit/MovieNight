package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Event implements Serializable {

	// JSON keys
	static final String ID = "id";
	static final String MOVIE = "movie";
	static final String SHOWTIME = "showtime";
	static final String THEATER = "theater";
	static final String ADMIN = "admin";
	static final String GUESTS = "guests";

	// object variables
	int id;
	Movie movie;
	Showtime showtime;
	Theater theater;
	User admin;
	List<Guest> guests;

	public Event(JSONObject data) {
		id = JSON.getInt(data, ID);
		movie = new Movie(JSON.getJSONObject(data, MOVIE));
		showtime = new Showtime(JSON.getJSONObject(data, SHOWTIME));
		theater = new Theater(JSON.getJSONObject(data, THEATER));
		showtime = new Showtime(JSON.getJSONObject(data, SHOWTIME));
		admin = new User(JSON.getJSONObject(data, ADMIN));
		
		guests = new ArrayList<Guest>();
		JSONArray myGuests = JSON.getJSONArray(data, GUESTS);
		if (myGuests != null) {
			for (int i = 0; i < myGuests.length(); i++) {
				guests.add(new Guest(JSON.getJSONObject(myGuests, i)));
			}
		}
	}
	
	public Event(Movie movie, Theater theater, Showtime showtime) {
		id = 0; // get next ID from server
		this.movie = movie;
		this.theater = theater;
		this.showtime = showtime;
		admin = null; // @todo pass in or get current user
		this.guests = new ArrayList<Guest>();
	}
	
}
