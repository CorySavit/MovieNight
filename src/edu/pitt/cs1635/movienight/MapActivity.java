package edu.pitt.cs1635.movienight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;



public class MapActivity extends FragmentActivity implements LocationListener{

	 private ImageButton mBtnFind;
	 EditText etPlace;
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
	    
	    mBtnFind = (ImageButton) findViewById(R.id.location_submit);
	    etPlace = (EditText) findViewById(R.id.location);
	    
	    mBtnFind.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String location = etPlace.getText().toString();
				
				if(location == null || location.equals("")){
					Toast.makeText(getBaseContext(), "No Location Entered", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String url = "https://maps.googleapis.com/maps/api/geocode/json?";
				
				try {
					location = URLEncoder.encode(location, "utf-8");
				} catch (UnsupportedEncodingException e){
					e.printStackTrace();
				}
				
				String address = "address=" + location;
				String sensor = "sensor=false";
				
				//url where geocoding data is fetched
				url = url + address + "&" + sensor;
				
				DownloadTask downloadTask = new DownloadTask();
				
				downloadTask.execute(url);
				
				
			}
	    	
	    });

	}
	
	private String downloadUrl(String strUrl) throws IOException{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(strUrl);
			
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			iStream = urlConnection.getInputStream();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
			StringBuffer sb = new StringBuffer();
			
			String line = "";
			while( ( line = br.readLine()) != null ){
				sb.append(line);
			}
			
			data = sb.toString();
			br.close();
		} catch(Exception e){
			Log.d("Exception while downloading url", e.toString());
		} finally{
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}
	
	private class DownloadTask extends AsyncTask<String, Integer, String>{

		String data = null;
		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			try{
				data = downloadUrl(url[0]);
			} catch(Exception e){
				Log.d("Background Task", e.toString());
			}
			return data;
		}
		
		protected void onPostExecute(String result){
			ParserTask parserTask = new ParserTask();
			
			parserTask.execute(result);
		}
		
	}
	
	class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>>{

		JSONObject jObject;
		
		@Override
		protected List<HashMap<String, String>> doInBackground(String... jsonData) {
			// TODO Auto-generated method stub
			List<HashMap<String, String>> places = null;
			GeocodeJSONParser parser = new GeocodeJSONParser();
			
			try{
				jObject = new JSONObject(jsonData[0]);
				
				places = parser.parse(jObject);
			} catch(Exception e){
				Log.d("Exception", e.toString());
			}
			return places;
		}
		
		protected void onPostExecute(List<HashMap<String, String>> list){
			mMap.clear();
			
			for(int i = 0;i<list.size();i++){
				//Creating a marker
				MarkerOptions markerOptions = new MarkerOptions();
				
				//Get a place
				HashMap<String, String> hmPlace = list.get(i);
				
				//Get latitude and logitude
				double lat = Double.parseDouble(hmPlace.get("lat"));
				double lng = Double.parseDouble(hmPlace.get("lng"));
				
				//Get name
				String name = hmPlace.get("formatted_address");
				
				LatLng latLng = new LatLng(lat, lng);
				
				markerOptions.position(latLng);
				markerOptions.title(name);
				
				mMap.addMarker(markerOptions);
				
				if(i==0){
					mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
				}
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case R.id.action_home:
			Intent homeView = new Intent(this, MainActivity.class);
			startActivity(homeView);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
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