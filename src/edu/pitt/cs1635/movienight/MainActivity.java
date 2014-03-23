package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import org.json.*;

import com.nostra13.universalimageloader.core.*;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

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
				
				// pass our serialized movie object to the activity
				// note that we should probably implement Parcelable instead
				// see http://www.developerphil.com/parcelable-vs-serializable/
				intent.putExtra("data", movie);
				
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
	        	.cacheOnDisc(false)
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
	        
	        // set title
	        // @todo figure out how to hide this if poster exists
	        //TextView title = (TextView) view.findViewById(R.id.title);
	        //title.setText(movie.title);
	 
	        // set poster image
	        // this ImageAware wrapper is supposed to solve the image re-load issue on scroll
	        // see https://github.com/nostra13/Android-Universal-Image-Loader/issues/406
	        ImageAware poster = new ImageViewAware((ImageView) view.findViewById(R.id.poster), false);
	        imageLoader.displayImage(movie.poster, poster, imageOptions);
	        
	        // set this view's tag to the entire data object
	        view.setTag(movie);
	        
	        return view;
	    }
	    
	}

}
