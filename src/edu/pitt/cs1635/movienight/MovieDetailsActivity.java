package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MovieDetailsActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);
        
        // getting intent data
        Intent intent = getIntent();
        
        // get JSON values from previous intent
        String id = intent.getStringExtra(MainActivity.TAG_ID);
        String title = intent.getStringExtra(MainActivity.TAG_TITLE);
        
        // @todo use id to make an API call to get additional data
        
        // set all values in view
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);
    }

}
