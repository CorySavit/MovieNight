package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

/*
 * Just a simple wrapper around User that keeps track of their status
 */
public class Guest implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String USER = "user";
	static final String STATUS = "status";
	
	// define status
	static final int STATUS_INVITED = 0;
	static final int STATUS_ACCEPTED = 1;
	static final int STATUS_DECLINED = -1;
	static final int STATUS_ADMIN = 2;

	// object variables
	User user;
	int status;

	public Guest(JSONObject data) {
		user = new User(JSON.getJSONObject(data, USER));
		status = JSON.getInt(data, STATUS);
	}
	
	public Guest(User user) {
		this.user = user;
		status = STATUS_INVITED;
	}
}
