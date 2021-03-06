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
	static final String SHOWTIME = "showtime_id";
	static final String ADMIN = "admin_id";
	static final String STATUS = "status";
	static final String GUESTS = "guests";
	
	// define status
	static final int STATUS_INVITED = Guest.STATUS_INVITED;
	static final int STATUS_ACCEPTED = Guest.STATUS_ACCEPTED;
	static final int STATUS_DECLINED = Guest.STATUS_DECLINED;
	static final int STATUS_ADMIN = Guest.STATUS_ADMIN;

	// object variables
	Integer id;
	Showtime showtime;
	User admin;
	Integer status;
	List<User> guests;

	public Event(JSONObject data) {
		id = JSON.getInt(data, ID);
		showtime = new Showtime(data);
		admin = new User(JSON.getJSONObject(data, ADMIN));
		status = JSON.getInt(data, STATUS);
		
		guests = new ArrayList<User>();
		JSONArray myGuests = JSON.getJSONArray(data, GUESTS);
		if (myGuests != null) {
			for (int i = 0; i < myGuests.length(); i++) {
				guests.add(new Guest(JSON.getJSONObject(myGuests, i)));
			}
		}
	}
	
	public Event(Showtime showtime) {
		this.showtime = showtime;
		admin = null; // @todo pass in or get current user
		status = STATUS_ADMIN; // @todo revisit this default value
		this.guests = new ArrayList<User>();
	}
	
	public Event(JSONObject data, Showtime showtime) {
		id = JSON.getInt(data, ID);
		status = JSON.getInt(data, STATUS);
		this.showtime = showtime;
	}
	
}
