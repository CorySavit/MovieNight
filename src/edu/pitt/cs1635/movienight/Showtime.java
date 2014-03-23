package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class Showtime implements Serializable {

	// JSON keys
	static final String TIME = "time";
	static final String FLAG = "flag";
	
	// define flags
	static final int FLAG_3D = 1;
	static final int FLAG_IMAX = 2;

	// object variables
	Date time;
	int flag;

	private SimpleDateFormat inputFormat;

	public Showtime(JSONObject data) {
		try {
			// parse time
			inputFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");
			time = inputFormat.parse(JSON.getString(data, TIME));
			
			// convert flag
			flag = JSON.getInt(data, FLAG);

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String getDate() {
		SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMMM d");
		return fmt.format(time);
	}
	
	public String toString() {
		SimpleDateFormat fmt = new SimpleDateFormat("h:mm a");
		String result = fmt.format(time);
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
