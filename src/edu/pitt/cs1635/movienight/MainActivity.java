package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
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
	private List<Movie> movieList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
				intent.putExtra(Movie.ID, movie.id);
				intent.putExtra(Movie.TITLE, movie.title);
				startActivity(intent);
			}
			
		});

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
			String str = API.getInstance().get("movies");

			if (str != null) {
				try {
					// parse result to a JSON Array
					JSONArray movies = new JSONArray(str);

					// loop through movies
					for (int i = 0; i < movies.length(); i++) {
						// create movie object from JSON data and add to list
						movieList.add(new Movie(movies.getJSONObject(i)));
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
			ListAdapter adapter = new MainActivityAdapter(MainActivity.this, movieList);
			posterGrid.setAdapter(adapter);
		}

	}

}
