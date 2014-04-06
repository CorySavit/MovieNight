package edu.pitt.cs1635.movienight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.json.*;

import com.nostra13.universalimageloader.core.*;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;


public class MainActivity extends Activity {

	private ProgressDialog pDialog;
	private GridView posterGrid;
	private List<Movie> movieList;
	public SharedPreferences prefs; //store zip?

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//get preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
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
		
		View locationBar = this.findViewById(R.id.curr_location_bar);
		locationBar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent mapIntent = new Intent(getApplicationContext(), MapActivity.class);
				startActivity(mapIntent);
				
			}
			
		});

		
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		Log.d("Latitude", String.valueOf(latitude));
		Log.d("Logitude", String.valueOf(longitude)); 
		Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

		
		TextView text = (TextView) findViewById(R.id.loc_display);
		 try {
		 List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

		 if(addresses != null) {
		 Address returnedAddress = addresses.get(0);
		 String zip = returnedAddress.getPostalCode();
		 /*StringBuilder strReturnedAddress = new StringBuilder();
		 for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
		 strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
		  }*/
		 Log.d("Returned Zip", zip);
		 text.setText(zip);
		 prefs.edit().putString("zip", zip);
		 Log.d("prefs in main: initial", prefs.getString("zip", "nothing there"));
		 }
		 else{
		 text.setText("No Address returned!");
		 }
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		text.setText("Canont get Address!");
		}
		

		// fetch our movies
		new GetMovies().execute();
		
		
		
		
		
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onResume();
		TextView text = (TextView) findViewById(R.id.loc_display);
		text.setText(prefs.getString("zip", "No Zip Exists"));
		
	}






	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_profile:
			return true;
		case R.id.action_map:
			Intent mapView = new Intent(this, MapActivity.class);
			startActivity(mapView);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		return true;
	}
	
	/*
	 * Asynchronous background task that fetches a list of movies from the API 
	 */
	private class GetMovies extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			// show progress dialog
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Popping popcorn...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			// make call to API
			String str = API.getInstance().get("movies");

			if (str != null) {
				try {
					// parse result to a JSON Object
					JSONObject movies = new JSONObject(str);

					// loop through movies
					Iterator<?> keys = movies.keys();
					while (keys.hasNext()) {
						String key = (String) keys.next();
						
						// create movie object from JSON data and add to list
						if (movies.get(key) instanceof JSONObject) {
							movieList.add(new Movie(movies.getJSONObject(key)));
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
	        ratingView.setText(Integer.toString(movie.mnRating));
	        ImageView ratingTag = (ImageView) view.findViewById(R.id.rating_tag);
	        if (movie.mnRating < 0) {
	        	ratingTag.setImageResource(R.drawable.negative_rating_tag);
	        } else if (movie.mnRating > 0) {
	        	ratingTag.setImageResource(R.drawable.positive_rating_tag);
	        } else {
	        	ratingTag.setImageResource(R.drawable.neutral_rating_tag);
	        }
	        
	        // set this view's tag to the entire data object
	        view.setTag(movie);
	        
	        return view;
	    }
	    
	    
	    
	}

}
