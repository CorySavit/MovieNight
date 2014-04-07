package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class CastMember extends Person implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String CHARACTER = "character";

	// object variables
	String character;

	public CastMember(JSONObject data) {
		super(data);
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		character = JSON.getString(data, CHARACTER);
	}

}
