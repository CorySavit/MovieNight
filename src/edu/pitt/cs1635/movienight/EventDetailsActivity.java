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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView.OnEditorActionListener;

public class EventDetailsActivity extends Activity {
	
	private LayoutInflater inflater;
	int eventID;
	private Event event;
	private Movie movie;
	private BaseAdapter messageAdapter;
	ArrayList<Message> messages;
	private AlertDialog.Builder statusBuilder;
	private TextView statusAdminView;
	private TextView statusGuestView;
	private TextView changeHost;
	private EditText newMessageEdit;
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
		messages = new ArrayList<Message>();
		
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
	            	
	            	if (event.status == null) {
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
		
		newMessageEdit = (EditText) findViewById(R.id.new_message);
		newMessageEdit.setOnEditorActionListener(new OnEditorActionListener() {
		
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					new PostMessage().execute();
		        }
		        return false;
			}
		});
		
		new GetEventInfo().execute();
		new GetEventMessages().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		if (session.isLoggedIn()) {
			menu.findItem(R.id.action_my_events).setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_profile:
			if (!session.isLoggedIn()){
				session.showLoginSignupDialog();
			} else {
				Intent profileView = new Intent(this, Profile.class);
				startActivity(profileView);
				return true;
			}
			return true;
		
		case R.id.action_my_events:
			startActivity(new Intent(this, MyEventsActivity.class));
			return true;
			
		case R.id.action_search:
			startActivity(new Intent(this, SearchActivity.class));
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setStatus(Integer status) {
		event.status = status;
		
		if (status != null) {
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
		}
		
		// show "Administrator" text or RSVP button accordingly
		if (status != null && status == Guest.STATUS_ADMIN && statusAdminView.getVisibility() == View.INVISIBLE) {
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
				guestTabView.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
						Intent intent = new Intent(getApplicationContext(), Profile.class);
						intent.putExtra(Profile.USER_ID, ((User) v.getTag()).id);
						startActivity(intent);
						return true;
					}
					
				});
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try{
		
			} finally {
				
			}
		
		}

	}
	
	private class PostMessage extends AsyncTask<Void, Void, Void> {
		
		private String message;
		
		protected Void doInBackground(Void... arg0) {
			
			message = newMessageEdit.getText().toString();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			params.add(new BasicNameValuePair("message", message));
            API.getInstance().post("events/"+eventID+"/messages", params);
            
			return null;
		}
		
		protected void onPostExecute(Void result){
			// update listview
            messages.add(new Message(message, session.getName(), "just now"));
            messageAdapter.notifyDataSetChanged();
            
            // clear edittext
			newMessageEdit.setText("");
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
					for (int i = 0; i < result.length(); i++) {					
						// create messages object from JSON data and add to list
						if (result.get(i) instanceof JSONObject) {
							
							Message temp = new Message(result.getJSONObject(i));
							Log.d("WHATEVER", temp.toString());
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
			messageAdapter = new MessageAdapter(EventDetailsActivity.this, messages);
			messagesView.setAdapter(messageAdapter);
		
		}

	}
	
	private class MessageAdapter extends BaseAdapter{
		 private ArrayList<Message> messageses;
		 private LayoutInflater inflater = null;
		 
		 private MessageAdapter(Activity a, ArrayList<Message> d) {
			 inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			 messageses = d;
			 
		 }

		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			super.notifyDataSetChanged();
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
	        
	        view.setTag(guest);
	        
	        return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}
	
	private class RSVP extends AsyncTask<Void, Void, Void> {
		
		private Integer status;
		
		public RSVP(Integer status) {
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
		
		private Integer status;
		
		public UpdateRSVP(Integer status) {
			this.status = status;
			Log.d("TEST", status + "");
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// user has already RSVP; they are changing it
    		List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			params.add(new BasicNameValuePair("status", Integer.toString(status)));
			String str = API.getInstance().put("events/" + eventID, params);
			Log.d("TEST", str);
			return null;
		}

	}

}
