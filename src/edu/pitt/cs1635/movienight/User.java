package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class User extends Person implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static final int STATUS_ADMIN = 1;
	static final int STATUS_ACCEPTED = 2;
	static final int STATUS_INVITED = 3;
	static final int STATUS_DECLINED = 4;

	// JSON keys
	static final String EMAIL = "email";
	static final String STATUS = "status";

	// object variables
	String email;
	int status;

	public User(JSONObject data) {
		super(data);
		
		// @todo sanity check for mock data
		if (data == null) {return;}
				
		email = JSON.getString(data, EMAIL);
		status = JSON.getInt(data, STATUS);
	}
}

