package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import org.json.*;

import com.nostra13.universalimageloader.core.*;

public class MainActivity extends Activity {

	private ProgressDialog pDialog;
	
	private GridView posterGrid;

	private ArrayList<HashMap<String, String>> movieList;
	
	static final String API_BASE_URL = "http://labs.amoscato.com/movienight-api/";
	
	// define JSON keys
	static final String TAG_TITLE = "title";
	static final String TAG_POSTER = "poster";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
		
        // create data structure
		movieList = new ArrayList<HashMap<String, String>>();
		
		// find grid view
		posterGrid = (GridView) this.findViewById(R.id.posterGrid);

		// fetch our movies
		new GetMovies().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
			ServiceHandler sh = new ServiceHandler();
			String str = sh.makeServiceCall(API_BASE_URL + "movies", ServiceHandler.GET);

			//Log.d("Response: ", "> " + str);

			if (str != null) {
				try {
					// parse result to a JSON Array
					JSONArray movies = new JSONArray(str);

					// loop through movies
					for (int i = 0; i < movies.length(); i++) {
						
						// get JSON movie object
						JSONObject c = movies.getJSONObject(i);
						
						// store its properties in a HashMap
						HashMap<String, String> movie = new HashMap<String, String>();
						movie.put(TAG_TITLE, c.getString(TAG_TITLE));
						movie.put(TAG_POSTER, c.getString(TAG_POSTER));
						
						// add this HashMap to the movie list
						movieList.add(movie);
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
			
			// update the grid view with via our LazyAdapter
			ListAdapter adapter = new LazyAdapter(MainActivity.this, movieList);
			posterGrid.setAdapter(adapter);
		}

	}

}
