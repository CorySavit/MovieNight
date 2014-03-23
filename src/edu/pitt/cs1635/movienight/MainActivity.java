package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.*;

import com.nostra13.universalimageloader.core.*;


public class MainActivity extends Activity {

	private ProgressDialog pDialog;
	private GridView posterGrid;
	private ArrayList<HashMap<String, String>> movieList;
	
	// define JSON keys
	static final String TAG_ID = "id";
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
		
		// listen for clicks on the grid view and delegate to each cell
		posterGrid.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				// getting values from selected movie
				String name = ((TextView) view.findViewById(R.id.title)).getText().toString();

				// start single contact activity
				Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
				intent.putExtra(TAG_ID, (String)view.getTag());
				intent.putExtra(TAG_TITLE, name);
				startActivity(intent);
			}
			
		});

		// fetch our movies
		new GetMovies().execute();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
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
			String str = API.getInstance().get("movies");

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
