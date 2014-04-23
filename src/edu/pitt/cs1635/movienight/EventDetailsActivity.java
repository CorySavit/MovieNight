package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class EventDetailsActivity extends Activity {
	
	private LayoutInflater inflater;
	int eventID;
	private Event event;
	private Movie movie;
	List<Message> messages;
	private AlertDialog.Builder statusBuilder;
	private TextView statusAdminView;
	private TextView statusGuestView;
	private TextView changeHost;
	private SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		session = new SessionManager(this);
		
		// getting intent data
		Intent intent = getIntent();
		movie = (Movie) intent.getSerializableExtra("movie");
		eventID = intent.getIntExtra("eventID", -1);
		
		// set header content
		if (movie != null) {
			movie.setHeader(this);
		}
		
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
		changeHost = (TextView) findViewById(R.id.change_host);
		statusAdminView = (TextView) findViewById(R.id.status_admin);
		statusGuestView = (TextView) findViewById(R.id.status_guest);
		statusGuestView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (session.isLoggedIn()) {
					statusBuilder.create().show();
				} else {
					// @todo after logging in, the activity should ideally refresh
					session.showLoginSignupDialog();
				}
			}

		});
		
		new GetEventInfo().execute();
		new GetEventMessages().execute();
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
		case Guest.STATUS_ACCEPTED:
			statusGuestView.setText(R.string.attending);
			break;
		case Guest.STATUS_INVITED:
			statusGuestView.setText(R.string.maybe);
			break;
		case Guest.STATUS_DECLINED:
			statusGuestView.setText(R.string.declined);
			break;
		}
		
		// show "Administrator" text or RSVP button accordingly
		if (status == Guest.STATUS_ADMIN && statusAdminView.getVisibility() == View.INVISIBLE) {
			statusGuestView.setVisibility(View.INVISIBLE);
			changeHost.setVisibility(View.VISIBLE);
			statusAdminView.setVisibility(View.VISIBLE);
		} else if (statusGuestView.getVisibility() == View.INVISIBLE) {
			statusAdminView.setVisibility(View.INVISIBLE);
			changeHost.setVisibility(View.INVISIBLE);
			statusGuestView.setVisibility(View.VISIBLE);
		}
	}
	
	
	
	private class GetEventInfo extends AsyncTask<Void, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Void... arg0) {
			
			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
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
				Log.e("ServiceHandler", "Failed to receive data from URL for events");
			}
			
			

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			
			/*
			 * Move header
			 */
			
			if (movie == null) {
				movie = new Movie(JSON.getJSONObject(result, "movie"));
				movie.setHeader(EventDetailsActivity.this);
			}
			
			/*
			 * Event information tab
			 */
			
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
				TextView host = (TextView) findViewById(R.id.host);
				if (result.getInt("admin_id") == session.getId()) {
					// current user is the admin
					host.setText("Me");
				} else {
					// somebody else is the admin
					host.setText(result.getString("admin_name"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// set status
			setStatus(event.status);
			
			/*
			 * Guests tab
			 */
			
			try {
				// add status categories and guests to a data structure
				JSONObject statusList = result.getJSONObject("guests");
				int[] statusID = {Guest.STATUS_ACCEPTED, Guest.STATUS_INVITED, Guest.STATUS_DECLINED};
				int[] statusLabels = {R.string.attending, R.string.invited, R.string.declined};
				Map<String, List<Guest>> guestMap = new HashMap<String, List<Guest>>();
				for (int i = 0; i < statusID.length; i++) {
					List<Guest> guests = new ArrayList<Guest>();
					JSONArray guestList = statusList.getJSONArray(Integer.toString(statusID[i]));
					for (int j = 0; j < guestList.length(); j++) {
						Guest guest = new Guest(guestList.getJSONObject(j));
						guests.add(guest);
					}
					guestMap.put(getString(statusLabels[i]), guests);
				}
				
				// populate guest list expandablelistview
				ExpandableListView guestTabView = (ExpandableListView) findViewById(R.id.guests);
				guestTabView.setAdapter(new GuestAdapter(EventDetailsActivity.this, guestMap, statusLabels));
				guestTabView.expandGroup(0);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try{
		
			} finally {
				
			}
		
		}

	}
	
	private class GetEventMessages extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			
			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			String mess = API.getInstance().get("events/" + eventID +"/messages", params);
			
			if (mess != null){
				try {
					// parse result to a JSON Object
					JSONArray result = new JSONArray(mess);
					
					

					// loop through movies
					for (int i = 0; i < result.length()-1; i++) {					
						// create messages object from JSON data and add to list
						if (result.get(i) instanceof JSONObject) {
							
							Message temp = new Message(result.getJSONObject(i));
							Log.d("WHATEVER", temp.toString());
							//RIGHT HERE !! NULL POINTER! WHY?!
							messages.add(temp);
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
			ListView messagesView = (ListView) findViewById(R.id.messages);
			ListAdapter messageAdapter = new MessageAdapter(EventDetailsActivity.this, messages);
			messagesView.setAdapter(messageAdapter);
		
		}

	}
	
	private class MessageAdapter extends BaseAdapter{
		 private List<Message> messageses;
		 private LayoutInflater inflater = null;
		 
		 private MessageAdapter(Activity a, List<Message> d) {
			 inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			 messageses = d;
			 
		 }

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return messageses.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return messageses.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.message, null);
	        }
	        
	        Message message = messageses.get(position);
	        
	        TextView author = (TextView) view.findViewById(R.id.author);
	        TextView text = (TextView) view.findViewById(R.id.message);
	        TextView time = (TextView) view.findViewById(R.id.message_time);
	        author.setText(message.author);
	        text.setText(message.message);
	        time.setText(message.time);
	        
			return view;
		}
		
	}
	
	private class GuestAdapter extends BaseExpandableListAdapter {
		
		private Map<String, List<Guest>> data;
		private int[] statusLabels;
		private ImageLoader imageLoader;
	    private DisplayImageOptions imageOptions;
		
		public GuestAdapter(Activity a, Map<String, List<Guest>> map, int[] labels) {
			inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			data = map;
			statusLabels = labels;
			imageLoader = ImageLoader.getInstance();
	        imageOptions = new DisplayImageOptions.Builder()
	        	.showImageOnLoading(R.drawable.blank_profile)
	        	.showImageForEmptyUri(R.drawable.blank_profile)
	        	.showImageOnFail(R.drawable.blank_profile)
	        	.cacheInMemory(true)
	        	.cacheOnDisc(true)
	        	.build();
		}
		
		@Override
		public int getGroupCount() {
			return data.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return data.get(getGroupKey(groupPosition)).size();
		}
		
		public String getGroupKey(int pos) {
			return getString(statusLabels[pos]);
		}

		@Override
		public Object getGroup(int groupPosition) {
			return data.get(getGroupKey(groupPosition));
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return data.get(getString(statusLabels[groupPosition])).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.guest_status_group, parent, false);
			}
			
			// set title
	        TextView title = (TextView) view.findViewById(R.id.status);
	        title.setText(getGroupKey(groupPosition) + " (" + getChildrenCount(groupPosition) + ")");
			
			return view;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.user_item, parent, false);
			}
			
			Guest guest = (Guest) getChild(groupPosition, childPosition);
			
			// set title
	        TextView name = (TextView) view.findViewById(R.id.name);
	        name.setText(guest.name);
			
			// set profile image
	        ImageAware photo = new ImageViewAware((ImageView) view.findViewById(R.id.photo), false);
	        imageLoader.displayImage(guest.photo, photo, imageOptions);
	        
	        return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
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
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
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
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			params.add(new BasicNameValuePair("status", Integer.toString(status)));
			API.getInstance().put("events/" + eventID, params);
			return null;
		}

	}

}
