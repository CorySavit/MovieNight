package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import org.json.JSONObject;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// JSON keys
	static final String AUTHOR = "author";
	static final String MESSAGE = "message";
	static final String TIME = "time";
	
	String author;
	String message;
	String time;

	
	public Message(JSONObject data) {
		
		// @todo sanity check for mock data
		if (data == null) {return;}
		
		author = JSON.getString(data, AUTHOR);
		message = JSON.getString(data, MESSAGE);
		time = JSON.getString(data, TIME);
	}
	
	public Message(String message, String author, String time) {
		this.message = message;
		this.author = author;
		this.time = time;
	}
	
	public String toString() {
		return author + ": "+message+" ("+time+")";
	}
}