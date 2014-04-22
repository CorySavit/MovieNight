package edu.pitt.cs1635.movienight;

import java.io.Serializable;

import org.json.JSONObject;

public class SelectableUser extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	boolean selected;
	int order;

	public SelectableUser(JSONObject data, int order) {
		super(data);
		
		selected = false;
		this.order = order;
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

