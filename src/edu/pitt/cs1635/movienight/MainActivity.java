package edu.pitt.cs1635.movienight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.*;

import com.nostra13.universalimageloader.core.*;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;


public class MainActivity extends Activity {
	SessionManager session;
	private ProgressDialog pDialog;
	private GridView posterGrid;
	private List<Movie> movieList;
	private String locationString;
	private Double latitude;
	private Double longitude;
	public SharedPreferences settings;
	private TextView locationTextView;
	
	static final int SET_LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//get session instance
		session = new SessionManager(this);
		//get preferences (Location)
		settings = getPreferences(MODE_PRIVATE);
		
		// create progress dialog
		pDialog = new ProgressDialog(MainActivity.this);
		pDialog.setMessage("Popping popcorn...");
		pDialog.setCancelable(false);
		
		// create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
		
        // create data structure
		movieList = new ArrayList<Movie>();
		
		// find grid view
		posterGrid = (GridView) this.findViewById(R.id.posterGrid);
		
		// listen for clicks on the grid view and delegate to each cell
		posterGrid.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				// start movie details activity
				Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
				Movie movie = (Movie) view.getTag();
				
				// pass our serialized movie object to the activity
				// note that we should probably implement Parcelable instead
				// see http://www.developerphil.com/parcelable-vs-serializable/
				intent.putExtra("data", movie);
				
				startActivity(intent);
			}
			
		});
		
		// set location bar onclick listener
		View locationBar = this.findViewById(R.id.curr_location_bar);
		locationBar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
				startActivityForResult(mapIntent, SET_LOCATION);
			}
			
		});

		// find location via GPS
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		locationTextView = (TextView) findViewById(R.id.loc_display);
		
		latitude = null;
		longitude = null;
		locationString = null;
		
		if (location != null) {
			longitude = location.getLongitude();
			latitude = location.getLatitude(); 
			
			// get location every time the app opens
			locationString = formatAddress(location.getLatitude(), location.getLongitude());
			
			// if we can't get address, try to get last saved address
			locationString = (locationString == null) ? settings.getString(SessionManager.LOCATION, getString(R.string.unknown_location)) : locationString;
			latitude = (Double) ((latitude == null) ? settings.getFloat(SessionManager.LAT, 0) : latitude);
			longitude = (Double) ((longitude == null) ? settings.getFloat(SessionManager.LNG, 0) : longitude);
			locationTextView.setText(locationString);
		}
		
		// fetch our movies
		new GetMovies().execute(latitude, longitude);
		if (session.isLoggedIn()){
			new GetPastMovies().execute();
		}
		
	}
	
	public String formatAddress(double lat, double lng) {
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
		try {
			List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
			return addresses != null ? formatAddress(addresses.get(0)) : null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String formatAddress(Address address) {
		String result = address.getLocality();
		if (result != null) {
			// append state abbreviation if locality exists
			result += ", " + address.getAdminArea();
		} else {
			// fallback on zip code
			result = address.getPostalCode();
		}
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == SET_LOCATION && resultCode == RESULT_OK) {
			latitude = data.getDoubleExtra(SessionManager.LAT, 0);
			longitude = data.getDoubleExtra(SessionManager.LNG, 0);
			new GetMovies().execute(latitude, longitude);
			locationTextView.setText(formatAddress(latitude, longitude));
		}
		
	}

	@Override
	protected void onStop(){
		super.onStop();
		// save location when the app stops
		if (latitude != null && longitude != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(SessionManager.LOCATION, locationString);
			editor.putFloat(SessionManager.LAT, new Float(latitude));
			editor.putFloat(SessionManager.LNG, new Float(longitude));
			editor.commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		if (session.isLoggedIn()) {
			menu.findItem(R.id.action_my_events).setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_profile:
			if (!session.isLoggedIn()){
				session.showLoginSignupDialog();
			} else {
				Intent profileView = new Intent(this, Profile.class);
				startActivity(profileView);
				return true;
			}
			return true;
		
		case R.id.action_my_events:
			startActivity(new Intent(this, MyEventsActivity.class));
			return true;
			
		case R.id.action_search:
			startActivity(new Intent(this, SearchActivity.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Asynchronous background task that fetches a list of movies from the API
	 * Note that execute should be passed two parameters: a lat and lng respectively
	 */
	private class GetMovies extends AsyncTask<Double, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// show progress dialog
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Double... geo) {
			
			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			if (geo.length == 2 && geo[0] != null && geo[1] != null) {
				params.add(new BasicNameValuePair("lat", Double.toString(geo[0])));
				params.add(new BasicNameValuePair("lng", Double.toString(geo[1])));
			}
			String str = API.getInstance().get("movies", params);

			if (str != null) {
				try {
					// parse result to a JSON Object
					JSONArray movies = new JSONArray(str);

					// loop through movies
					for (int i = 0; i < movies.length(); i++) {
						
						// create movie object from JSON data and add to list
						if (movies.get(i) instanceof JSONObject) {
							movieList.add(new Movie(movies.getJSONObject(i)));
						}
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// dismiss the progress dialog
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
			
			// update the grid view with via our PosterAdapter
			ListAdapter adapter = new PosterAdapter(MainActivity.this, movieList);
			posterGrid.setAdapter(adapter);
		}

	}
	
	/*
	 * Used to populate the GridView of movie posters
	 */
	private class PosterAdapter extends BaseAdapter {
		
		private Activity activity;
	    private List<Movie> movies;
	    private LayoutInflater inflater = null;
	    private ImageLoader imageLoader;
	    private DisplayImageOptions imageOptions;
	 
	    private PosterAdapter(Activity a, List<Movie> d) {
	        activity = a;
	        movies = d;
	        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        imageLoader = ImageLoader.getInstance();
	        imageOptions = new DisplayImageOptions.Builder()
	        	.showImageOnLoading(R.drawable.blank_poster)
	        	.showImageForEmptyUri(R.drawable.blank_poster)
	        	.showImageOnFail(R.drawable.blank_poster)
	        	.cacheInMemory(true)
	        	.cacheOnDisc(true)
	        	.build();
	    }
	    
	    @Override
		public int getCount() {
			return movies.size();
		}

		@Override
		// we have to implement this
		public Object getItem(int position) {
			return movies.get(position);
		}

		@Override
		// again, we have have to implement this
		public long getItemId(int position) {
			return position;
		}
	 
	    // see http://developer.android.com/reference/android/widget/Adapter.html#getView(int, android.view.View, android.view.ViewGroup)
	    // this method is called for every grid in the gridview
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	// create a new view if old view does not exist
	    	View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.movie_poster, null);
	        }
	        
	        // get appropriate movie data
	        Movie movie = movies.get(position);
	 
	        // set poster image
	        // this ImageAware wrapper is supposed to solve the image re-load issue on scroll
	        // see https://github.com/nostra13/Android-Universal-Image-Loader/issues/406
	        ImageAware poster = new ImageViewAware((ImageView) view.findViewById(R.id.poster), false);
	        imageLoader.displayImage(movie.poster, poster, imageOptions);
	        
	        // movienight rating
	        TextView ratingView = (TextView) view.findViewById(R.id.mn_rating);
	        if (movie.mnRating != null) {
	        	ratingView.setText(Integer.toString(movie.mnRating) + "%");
		        ImageView ratingTag = (ImageView) view.findViewById(R.id.rating_tag);
		        if (movie.mnRating < 50) {
		        	ratingTag.setImageResource(R.drawable.negative_rating_tag);
		        } else {
		        	ratingTag.setImageResource(R.drawable.positive_rating_tag);
		        }
		        ((RelativeLayout) view.findViewById(R.id.rating_tag_wrap)).setVisibility(View.VISIBLE);
	        }
	        
	        // set this view's tag to the entire data object
	        view.setTag(movie);
	        
	        return view;
	    }
	    
	    
	    
	}
	
	private class GetPastMovies extends AsyncTask<Void, Void, JSONObject> {

		

		@Override
		protected JSONObject doInBackground(Void... args) {
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			String pastMovies = API.getInstance().get("events/past", params);
			if (pastMovies != null) {
				try {
					JSONArray result = new JSONArray(pastMovies);
					if(result.get(0) instanceof JSONObject){
						JSONObject temp = result.getJSONObject(0);
						return temp;
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL for events");
			}

			return null;
		}

		@Override
		protected void onPostExecute(final JSONObject result) {
			super.onPostExecute(result);
			if (result != null){
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
				final View ratingView =  inflater.inflate(R.layout.dialog_rating, null);

				final AlertDialog rating = new AlertDialog.Builder(MainActivity.this)
					.setView(ratingView)
					.create();
				rating.setCancelable(false);
				TextView title = (TextView) ratingView.findViewById(R.id.ratingMovieTitle);
				String setTitle;
				try {
					setTitle = result.getString("title");
					title.setText(setTitle);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				((ImageButton) ratingView.findViewById(R.id.thumbsup)).setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// Submit Rating
						try {
							new SubmitRating().execute(Integer.toString(session.getId()), result.getString("movie_id"), result.getString("event_id"), "1");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						
						
						// Update Users2Events
						rating.dismiss();

					}

				});

				((ImageButton) ratingView.findViewById(R.id.thumbsdown)).setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						try {
							new SubmitRating().execute(Integer.toString(session.getId()), result.getString("movie_id"), result.getString("event_id"), "0");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						rating.dismiss();
					}
					

				});
				rating.show();
			}
		
		}
		
		

	}
	
private class SubmitRating extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... args) {
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", args[0]));
			params.add(new BasicNameValuePair("rating", args[3]));
			
			String submitRating = API.getInstance().post("movies/"+args[1]+"/rating", params);
			if (submitRating != null) {
				try {
					Log.d("SOMETHING", submitRating);
					JSONObject result = new JSONObject(submitRating);
					Log.d("SOMETHING", result.getString("success"));
					if(result.getInt("success") == 1){
						List<NameValuePair> params2 = new ArrayList<NameValuePair>();
						params2.add(new BasicNameValuePair("user_id", args[0]));
						params2.add(new BasicNameValuePair("event_id", args[2]));
						API.getInstance().put("events/past", params2);
						
					} else {
						//error submitting rating
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL for events");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
}
	
	


}
