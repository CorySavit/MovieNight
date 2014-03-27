package edu.pitt.cs1635.movienight;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;



public class MapActivity extends Activity {
	
	private GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        // Getting GoogleMap object from the fragment

        // Enabling MyLocation Layer of Google Map
        map.setMyLocationEnabled(true);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}