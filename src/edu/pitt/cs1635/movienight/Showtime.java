package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

public class Showtime implements Serializable {
	private static final long serialVersionUID = 1L;

	// JSON keys
	static final String ID = "id";
	static final String TIME = "time";
	static final String FLAG = "flag";
	static final String TICKET_URL = "ticket_url";
	static final String THEATER_ID = "theater_id";
	static final String THEATER_NAME = "theater_name";
	static final String THEATER_ADDRESS = "address";
	
	// define flags
	static final int FLAG_3D = 1;
	static final int FLAG_IMAX = 2;

	// object variables
	int id;
	Movie movie;
	Theater theater;
	Date time;
	int flag;
	String ticketURL;

	private static SimpleDateFormat inputFormat;

	public Showtime(JSONObject data) {
		// parse time
		inputFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm", Locale.US);
		time = parseDate(JSON.getString(data, TIME));
		
		id = JSON.getInt(data, ID);
		flag = JSON.getInt(data, FLAG);
		ticketURL = JSON.getString(data, TICKET_URL);
		
		// @todo this is tailored for movie/event details featured events tab right now
		theater = new Theater(JSON.getInt(data, THEATER_ID), JSON.getString(data, THEATER_NAME), JSON.getString(data, THEATER_ADDRESS));
	}
	
	public static Date parseDate(String date) {
		try {
			return inputFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDate() {
		return getDate(time);
	}
	
	public static String getDate(Date time) {
		SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
		return fmt.format(time);
	}
	
	public String getTime() {
		return getTime(time);
	}
	
	public static String getTime(Date time) {
		SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.US);
		return fmt.format(time);
	}
	
	public String toString() {
		String result = getTime();
		switch(flag) {
			case FLAG_3D:
				result += " (3D)";
				break;
			case FLAG_IMAX:
				result += " (IMAX)";
				break;
			default:
				// do nothing
				break;
		}
		return result;
	}

}
