package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Theater implements Serializable {

	// JSON keys
	static final String ID = "id";
	static final String NAME = "name";
	static final String SHOWTIMES = "showtimes";
	static final String TICKET_URL = "ticketurl";

	// object variables
	int id;
	String name;
	List<Showtime> showtimes;
	String ticketurl;

	private SimpleDateFormat inputFormat;

	public Theater(JSONObject data) {
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		name = JSON.getString(data, NAME);
		ticketurl = JSON.getString(data, TICKET_URL);

		// add showtimes
		inputFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
		showtimes = new ArrayList<Showtime>();
		JSONArray myShowtimes = JSON.getJSONArray(data, SHOWTIMES);
		for (int i = 0; i < myShowtimes.length(); i++) {
			showtimes.add(new Showtime(JSON.getJSONObject(myShowtimes, i)));
		}

	}
	
}
