package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Event implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String ID = "id";
	static final String MOVIE = "movie";
	static final String SHOWTIME = "showtime";
	static final String THEATER = "theater";
	static final String ADMIN = "admin";
	static final String STATUS = "status";
	static final String GUESTS = "guests";
	
	// define status
	static final int STATUS_INVITED = Guest.STATUS_INVITED;
	static final int STATUS_ACCEPTED = Guest.STATUS_ACCEPTED;
	static final int STATUS_DECLINED = Guest.STATUS_DECLINED;
	static final int STATUS_ADMIN = Guest.STATUS_ADMIN;

	// object variables
	int id;
	Movie movie;
	Showtime showtime;
	Theater theater;
	User admin;
	int status;
	List<Guest> guests;

	public Event(JSONObject data) {
		id = JSON.getInt(data, ID);
		movie = new Movie(JSON.getJSONObject(data, MOVIE));
		showtime = new Showtime(JSON.getJSONObject(data, SHOWTIME));
		theater = new Theater(JSON.getJSONObject(data, THEATER));
		admin = new User(JSON.getJSONObject(data, ADMIN));
		status = JSON.getInt(data, STATUS);
		
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
		status = STATUS_ADMIN; // @todo revisit this default value
		this.guests = new ArrayList<Guest>();
	}
	
}
