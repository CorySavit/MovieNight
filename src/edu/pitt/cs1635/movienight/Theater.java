package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class Theater implements Serializable {
	private static final long serialVersionUID = 1L;

	// JSON keys
	static final String ID = "id";
	static final String NAME = "name";
	static final String ADDRESS = "address";
	static final String SHOWTIMES = "showtimes";

	// object variables
	int id;
	String name;
	String address;
	List<Showtime> showtimes;

	private SimpleDateFormat inputFormat;

	public Theater(JSONObject data) {
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		name = JSON.getString(data, NAME);
		address = JSON.getString(data, ADDRESS); 

		// add showtimes
		inputFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.US);
		showtimes = new ArrayList<Showtime>();
		JSONArray myShowtimes = JSON.getJSONArray(data, SHOWTIMES);
		for (int i = 0; i < myShowtimes.length(); i++) {
			showtimes.add(new Showtime(JSON.getJSONObject(myShowtimes, i)));
		}
	}
	
	public Theater(int id, String name, String address) {
		this.id = id;
		this.name = name;
		this.address = address;
	}
	
	public String toString() {
		return name;
	}
	
}
