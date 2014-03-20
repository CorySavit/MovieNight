package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MovieDetailsActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details);
        
        // getting intent data
        Intent intent = getIntent();
        Movie movie = (Movie) intent.getSerializableExtra("data");
        
        // @todo use id to make an API call to get additional data?
        
        // set all values in view
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(movie.title);
    }

}
