package edu.pitt.cs1635.movienight;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyEventsActivity extends Activity {
	
	private ArrayList<Event> events;
	private EventAdapter eventAdapter;
	private ListView eventsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		
		// get intent data
		Intent intent = getIntent();
		events = new ArrayList<Event>();
		
		// @todo we will eventually need to post this to the server as well
		// @todo we need to make sure we are sorting these events by date (this might not be the next event)
		events.add((Event) intent.getSerializableExtra("data"));
		
		// display toast notification
		Toast.makeText(getApplicationContext(), "Created " + events.get(0).movie.title + " MovieNight!", Toast.LENGTH_LONG).show();
		
		// get the rest of my events
		new GetEvents().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
 
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_events, menu);
		return true;
	}
	
	/*
	 * Asynchronous background task that fetches a list of my events from the API 
	 */
	private class GetEvents extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			// make call to API
			String str = API.getInstance().get("events");
			System.out.println(str);

			if (str != null) {
				try {
					// parse result to a JSON Array
					JSONArray data = new JSONArray(str);

					// loop through friends
					for (int i = 0; i < data.length(); i++) {
						Event event = new Event(data.getJSONObject(i));
						events.add(event);
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
			
			eventsView = (ListView) findViewById(R.id.events);
			eventAdapter = new EventAdapter(MyEventsActivity.this, R.layout.event_item, events);
			eventsView.setAdapter(eventAdapter);
		}
		
	}
	
	/*
	 * Used to populate the ListView of friends
	 */
	private class EventAdapter extends ArrayAdapter<Event> {
		
		private ArrayList<Event> events;
	    private LayoutInflater inflater = null;
	    
	    public EventAdapter(Context context, int textViewResourceId, ArrayList<Event> list) {
			super(context, textViewResourceId, list);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			events = list;
		}
	 
	    // this method is called for every item in the listview
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	// create a new view if old view does not exist
	    	View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.event_item, null);
	        }
	        
	        Event event = events.get(position);
	        
	        // set title
	        TextView title = (TextView) view.findViewById(R.id.title);
	        title.setText(event.movie.title);
	        
	        // set subtitle
	        TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
	        int num = event.guests.size();
	        subtitle.setText("with " + event.guests.size() + " friend" + (num == 1 ? "" : "s") + " on " + event.showtime.getDate() + " at " + event.showtime);
	        
	        // set this view's tag to the entire data object
	        view.setTag(event);
	        
	        return view;
	    }
	    
	}

}
