package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class EventDetailsActivity extends Activity {
	
	private LayoutInflater inflater;
	int eventID;
	private Event event;
	private Movie movie;

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
			
			Spinner spinner = (Spinner) findViewById(R.id.status_spinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EventDetailsActivity.this, R.array.status_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
		}

	}

}
