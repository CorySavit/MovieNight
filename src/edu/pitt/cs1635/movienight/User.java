package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class User extends Person implements Serializable {
	private static final long serialVersionUID = 1L;

	// JSON keys
	static final String EMAIL = "email";

	// object variables
	String email;
	boolean selected;

	public User(JSONObject data) {
		super(data);
		
		// @todo sanity check for mock data
		if (data == null) {return;}
				
		email = JSON.getString(data, EMAIL);
		selected = false;
	}
	
	// toggle selected state
	public void toggle() {
		if (selected) {
			selected = false;
		} else {
			selected = true;
		}
	}

}

