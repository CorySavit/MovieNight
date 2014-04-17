package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class CastMember extends Person implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String NAME = "name";
	static final String CHARACTER = "character";
	static final String PHOTO = "photo";

	// object variables
	String name;
	String character;
	String photo;

	public CastMember(JSONObject data) {
		super(data);
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		name = JSON.getString(data, NAME);
		character = JSON.getString(data, CHARACTER);
		photo = JSON.getString(data, PHOTO);
	}
	
	/*
	@Override
	public String toString() {
		return this.name + " (as " + this.character + ")";
	}
	*/

}
