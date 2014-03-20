package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Theater implements Serializable {
	
	// JSON keys
	static final String NAME = "name";
	static final String SHOWTIMES = "showtimes";
	
	// object variables
	String name;
	List<Date> showtimes;
	
	private SimpleDateFormat inputFormat;
	
	public Theater(JSONObject data) {
		try {
			name = data.getString(NAME);
			
			// add showtimes
			inputFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
			showtimes = new ArrayList<Date>();
			JSONArray myShowtimes = data.getJSONArray(SHOWTIMES);
			for (int i = 0; i < myShowtimes.length(); i++) {
				showtimes.add(inputFormat.parse(myShowtimes.getString(i)));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	// @todo should probably add get methods
}
