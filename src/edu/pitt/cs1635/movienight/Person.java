package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String ID = "id";
	static final String NAME = "name";
	static final String PHOTO = "photo";
	
	Integer id;
	String name;
	String photo;
	
	public Person(JSONObject data) {
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		name = JSON.getString(data, NAME);
		photo = JSON.getString(data, PHOTO);
		
	}
	
	public String toString() {
		return name;
	}
}
