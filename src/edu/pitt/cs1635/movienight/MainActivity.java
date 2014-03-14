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

public class MainActivity extends Activity {

	private ProgressDialog pDialog;
	
	private GridView posterGrid;

	private ArrayList<HashMap<String, String>> movieList;
	private static String url = "http://labs.amoscato.com/movienight-api/movies";
	private static final String TAG_ID = "rootId";
	private static final String TAG_TITLE = "title";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		movieList = new ArrayList<HashMap<String, String>>();
		
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
			
			ServiceHandler sh = new ServiceHandler();
			String str = sh.makeServiceCall(url, ServiceHandler.GET);

			Log.d("Response: ", "> " + str);

			if (str != null) {
				try {
					JSONArray movies = new JSONArray(str);

					// loop through movies
					for (int i = 0; i < movies.length(); i++) {
						
						JSONObject c = movies.getJSONObject(i);
						String id = c.getString(TAG_ID);
						String title = c.getString(TAG_TITLE);
						
						HashMap<String, String> movie = new HashMap<String, String>();
						movie.put(TAG_ID, id);
						movie.put(TAG_TITLE, title);
						
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
				
			ListAdapter adapter = new SimpleAdapter(
					MainActivity.this, movieList,
					R.layout.movie_poster, new String[] { TAG_TITLE }, new int[] { R.id.title });

			posterGrid.setAdapter(adapter);
		}

	}

}
