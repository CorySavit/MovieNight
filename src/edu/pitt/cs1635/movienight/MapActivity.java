package edu.pitt.cs1635.movienight;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;



public class MapActivity extends FragmentActivity implements LocationListener{

	 private GoogleMap mMap;
	 private LocationManager locationManager;
	 private static final long MIN_TIME = 400;
	 private static final float MIN_DISTANCE = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_map);

	    if (mMap == null) {
	        // Try to obtain the map from the SupportMapFragment.
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	                .getMap();
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
	        }
	    }
	    mMap.setMyLocationEnabled(true);

	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); 


	}

	@Override
	public void onLocationChanged(Location location) {
	    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
	    mMap.animateCamera(cameraUpdate);
	    locationManager.removeUpdates(this);

	}

	@Override
	public void onProviderDisabled(String provider) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	    // TODO Auto-generated method stub

	}
}