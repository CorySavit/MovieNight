package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MyEventsActivity extends Activity {
	
	private Event newEvent;
	private EventAdapter eventAdapter;
	private ListView eventsView;
	private SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_events);
		
		session = new SessionManager(this);
		
		new GetEvents().execute();
		
		// check to see if we are adding event
		Intent intent = getIntent();
		newEvent = (Event) intent.getSerializableExtra("data");
		if (newEvent != null) {
			Toast.makeText(getApplicationContext(), "Created " + newEvent.showtime.movie.title + " MovieNight!", Toast.LENGTH_LONG).show();
			User[] guests = new User[newEvent.guests.size()];
			for (int i = 0; i < newEvent.guests.size(); i++) {
				guests[i] = newEvent.guests.get(i);
			}
			new AddGuests().execute(guests);
		}
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
	private class GetEvents extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected JSONArray doInBackground(Void... arg0) {
			
			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			String str = API.getInstance().get("events", params);

			if (str != null) {
				try {
					// parse result to a JSON Array
					return new JSONArray(str);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(JSONArray result) {
			super.onPostExecute(result);
			
			eventsView = (ListView) findViewById(R.id.events);
			eventAdapter = new EventAdapter(MyEventsActivity.this, result);
			eventsView.setAdapter(eventAdapter);
			
			eventsView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(view.getContext(), EventDetailsActivity.class);
					intent.putExtra("eventID", (Integer) view.getTag());
					startActivity(intent);
				}
				
			});
		}
		
	}
	
	private class AddGuests extends AsyncTask<User, Void, Void> {

		@Override
		protected Void doInBackground(User... users) {
			
			for (int i = 0; i < users.length; i++) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_id", Integer.toString(users[i].id)));
				params.add(new BasicNameValuePair("status", Integer.toString(Guest.STATUS_INVITED)));
				API.getInstance().post("events/" + newEvent.id, params);
				// @todo catch error?
			}
			return null;
			
		}
		
	}
	
	/*
	 * Used to populate the ListView of friends
	 */
	private class EventAdapter extends BaseAdapter {
		
		private JSONArray events;
	    private LayoutInflater inflater = null;
	    
	    public EventAdapter(Context context, JSONArray events) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.events = events;
		}
	    
	    @Override
		public int getCount() {
			return events.length();
		}

		@Override
		public Object getItem(int position) {
			try {
				return events.getJSONObject(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	 
	    // this method is called for every item in the listview
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	// create a new view if old view does not exist
	    	View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.event_item, null);
	        }
	        
	        JSONObject event = (JSONObject) getItem(position);
	        
	        try {
	        
		        // set title
		        TextView title = (TextView) view.findViewById(R.id.title);
		        title.setText(event.getString(Movie.TITLE));
		        
		        // set subtitle
		        TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
		        JSONArray guests = event.getJSONArray(Event.GUESTS);
		        int num = guests.length();
		        Date time = Showtime.parseDate(event.getString(Showtime.TIME));
		        subtitle.setText("with " + num + " friend" + (num == 1 ? "" : "s") + " on " + Showtime.getDate(time) + " at " + Showtime.getTime(time));
		        
		        // set status icon
		        ImageView status = (ImageView) view.findViewById(R.id.status);
		        switch(event.getInt(Event.STATUS)) {
		        	case Event.STATUS_DECLINED:
		        		status.setImageResource(R.drawable.status_declined);
		        		title.setTextColor(getResources().getColor(R.color.gray));
		        		subtitle.setTextColor(getResources().getColor(R.color.gray));
		        		break;
		        	case Event.STATUS_INVITED:
		        		status.setImageResource(R.drawable.status_invited);
		        		break;
		        	default:
		        		status.setImageResource(R.drawable.status_accepted);
		        		break;
		        }
		        
		        // set this view's tag to the entire data object
		        view.setTag(event.getInt(Event.ID));
		        
	        } catch (JSONException e) {
				e.printStackTrace();
			}
	        
	        return view;
	    }
	    
	}

}
