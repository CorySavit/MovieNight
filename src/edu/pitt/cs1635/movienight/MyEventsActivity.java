package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MyEventsActivity extends Activity {
	
	private Event event;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		
		// get intent data
		Intent intent = getIntent();
		event = (Event) intent.getSerializableExtra("data");
		
		TextView temp = (TextView) findViewById(R.id.temp);
		temp.setText(event.guests.size() + " guests!");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
 
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_events, menu);
		return true;
	}

}
