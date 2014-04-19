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
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
	private String zipCode;
	
	public SharedPreferences settings;
	public static final String ZIP_CODE = "zipCode";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		session = new SessionManager(getApplicationContext());
		//get preferences
		settings = getPreferences(MODE_PRIVATE);
		
		
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
				startActivity(mapIntent);
			}
			
		});

		// find location via GPS
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		if (location != null) {
			double longitude = location.getLongitude();
			double latitude = location.getLatitude(); 
			Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
			
			// get location every time the app opens
			try {
				List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

				if (addresses != null) {
					Address returnedAddress = addresses.get(0);
					zipCode = returnedAddress.getPostalCode();
					/*
						StringBuilder strReturnedAddress = new StringBuilder();
						for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
							strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
						}
					*/
				}
				else{
					// if we can't get address, try to get last saved address
					zipCode = settings.getString(ZIP_CODE, getString(R.string.unknown_location));
				}
			} catch (IOException e) {
				e.printStackTrace();
				// if we can't get address, try to get last saved address
				zipCode = settings.getString(ZIP_CODE, getString(R.string.unknown_location));
			}
			TextView text = (TextView) findViewById(R.id.loc_display);
			text.setText(zipCode);
			
		}

		// fetch our movies
		new GetMovies().execute();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		
		// save location when the app stops
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(ZIP_CODE, zipCode);
		editor.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_profile:
			
			
			if(!session.isLoggedIn()){
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle("Sign In / Sign Out");
	 
				// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("Sign In",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, close
							// current activity
							Log.d("SignInClicked", "True");
							
							dialog.cancel();
							AlertDialog alert = loginDialog(MainActivity.this, "Enter you email and password");
							alert.show();
						}
					  })
					.setNegativeButton("Sign Up",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
							startActivity(intent);

						}
					});
	 
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
	 
					// show it
					alertDialog.show();
			} else {
			Intent profileView = new Intent(this, Profile.class);
			startActivity(profileView);
			return true;
			}

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
	
	public AlertDialog loginDialog(Context c, String message) {
		Log.d("Made it to LoginDialog", "True");
	    LayoutInflater factory = LayoutInflater.from(c);           
	    final View textEntryView = factory.inflate(R.layout.activity_signin, null);
	    final AlertDialog.Builder failAlert = new AlertDialog.Builder(c);
	    failAlert.setTitle("Login Failed");
	    failAlert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Cancelled
	        }
	    });
	    AlertDialog.Builder alert = new AlertDialog.Builder(c);
	    alert.setTitle("Login");
	    alert.setMessage(message);
	    alert.setView(textEntryView);
	    alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	                final EditText emailInput = (EditText) textEntryView.findViewById(R.id.email_login);
	                final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.password_login);
	                session.login(emailInput.getText().toString(), passwordInput.getText().toString());
	        }
	    });
	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Canceled.
	        }
	    });
	    return alert.create();
	}
	


}
