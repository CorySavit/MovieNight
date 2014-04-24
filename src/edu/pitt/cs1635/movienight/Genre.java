package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import org.json.JSONObject;

public class Genre implements Serializable {
	private static final long serialVersionUID = 1L;

	// JSON keys
	static final String ID = "id";
	static final String NAME = "name";
	
	// object variables
	Integer id;
	String name;
	
	public Genre(JSONObject data) {

		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		name = JSON.getString(data, NAME);
		
	}
	
	public String toString() {
		return name;
	}
}
