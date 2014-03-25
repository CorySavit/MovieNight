package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class User implements Serializable {

	// JSON keys
	static final String ID = "id";
	static final String NAME = "name";
	static final String EMAIL = "email";
	static final String PHOTO = "photo";

	// object variables
	int id;
	String name;
	String email;
	String photo;
	boolean selected;

	public User(JSONObject data) {
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		name = JSON.getString(data, NAME);
		email = JSON.getString(data, EMAIL);
		photo = JSON.getString(data, PHOTO);
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

