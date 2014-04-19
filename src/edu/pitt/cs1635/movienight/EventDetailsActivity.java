package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class EventDetailsActivity extends Activity {
	
	private LayoutInflater inflater;
	int eventID;
	private Event event;
	private Movie movie;
	private AlertDialog.Builder statusBuilder;
	private TextView statusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// getting intent data
		Intent intent = getIntent();
		movie = (Movie) intent.getSerializableExtra("movie");
		eventID = intent.getIntExtra("eventID", -1);
		
		// set header content
		movie.setHeader(this);
		
		/*
		 * Setup tabs
		 */

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		final FrameLayout tabContent = tabHost.getTabContentView();
		
		// create tabs
		final String[] tabs = {"details", "guests", "messaging"};
		final int[] icons = {R.drawable.ic_action_information, R.drawable.ic_action_guests, R.drawable.ic_action_message};
		for (int i = 0; i < tabs.length; i++) {
			final View tabContentView = tabContent.getChildAt(i);
			TabSpec tabSpec = tabHost.newTabSpec(tabs[i]);
			tabSpec.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return tabContentView;
				}
			});
			tabSpec.setIndicator(null, getResources().getDrawable(icons[i]));
			tabHost.addTab(tabSpec);
		}

		// ensure that all but the first tab content are invisible
		for (int index = 1; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
		
		// create alert dialog for status RSVP
		statusBuilder = new AlertDialog.Builder(EventDetailsActivity.this)
			.setTitle(R.string.RSVP)
	        .setItems(R.array.status_array, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	int newStatus = 0;
	            	switch (which) {
	            	case 0:
	            		newStatus = 2;
	            		break;
	            	case 1:
	            		newStatus = 3;
	            		break;
	            	case 2:
	            		newStatus = 4;
	            		break;
	            	}
	            	
	            	if (event.status == 0) {
	        			new RSVP(newStatus).execute();
	            	} else {
	            		new UpdateRSVP(newStatus).execute();
	            	}
	            	setStatus(newStatus);
	            }
	        });
		
		// save reference to status view
		statusView = (TextView) findViewById(R.id.status);
		statusView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				statusBuilder.create().show();
			}

		});
		
		new GetEventInfo().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setStatus(int status) {
		event.status = status;
		switch (status) {
		case Guest.STATUS_ADMIN:
			statusView.setText(R.string.administrator);
			break;
		case Guest.STATUS_ACCEPTED:
			statusView.setText(R.string.attending);
			break;
		case Guest.STATUS_INVITED:
			statusView.setText(R.string.maybe);
			break;
		case Guest.STATUS_DECLINED:
			statusView.setText(R.string.declined);
			break;
		}
	}
	
	private class GetEventInfo extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Void... arg0) {
			
			// make call to API
			// @todo this is just using static user_id = 1 right now
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", "1"));
			String str = API.getInstance().get("events/" + eventID, params);

			if (str != null) {
				try {
					JSONObject result = new JSONObject(str);
					event = new Event(result, new Showtime(result));
					return result;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			
			// time
			TextView time = (TextView) findViewById(R.id.time);
			time.setText(event.showtime.toString());
			TextView date = (TextView) findViewById(R.id.date);
			date.setText(event.showtime.getDate());
			
			// location
			((TextView) findViewById(R.id.theater_name)).setText(event.showtime.theater.name);
			((TextView) findViewById(R.id.theater_address)).setText(event.showtime.theater.address);
			
			// host
			try {
				((TextView) findViewById(R.id.host)).setText(result.getString("admin_name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// set status
			setStatus(event.status);
		}

	}
	
	private class RSVP extends AsyncTask<Void, Void, Void> {
		
		private int status;
		
		public RSVP(int status) {
			this.status = status;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// user has already RSVP; they are changing it
    		// @todo change hardcoded user id
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", "1"));
			params.add(new BasicNameValuePair("status", Integer.toString(status)));
			API.getInstance().post("events/" + eventID, params);
			return null;
		}

	}
	
	private class UpdateRSVP extends AsyncTask<Void, Void, Void> {
		
		private int status;
		
		public UpdateRSVP(int status) {
			this.status = status;
			Log.d("TEST", status + "");
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// user has already RSVP; they are changing it
    		// @todo change hardcoded user id
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", "1"));
			params.add(new BasicNameValuePair("status", Integer.toString(status)));
			API.getInstance().put("events/" + eventID, params);
			return null;
		}

	}

}
