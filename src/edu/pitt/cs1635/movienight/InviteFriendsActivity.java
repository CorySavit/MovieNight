package edu.pitt.cs1635.movienight;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class InviteFriendsActivity extends Activity {
	
	private Event event;
	private ArrayList<User> originalFriendList;
	private ArrayList<User> workingFriendList;
	
	private ListView friendView;
	private EditText search;
	private FriendAdapter friendAdapter;
	
	// update friendList based on queried filter 
	private void filter(String query) {
		String q = query.toLowerCase();
		workingFriendList.clear();
		for (User friend : originalFriendList) {
			if (query.length() == 0 || friend.name.toLowerCase().contains(q)) {
				workingFriendList.add(friend);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_friends);
		
		// get intent data
		Intent intent = getIntent();
		event = (Event) intent.getSerializableExtra("data");
		
		// set listview and initialize data structures
		friendView = (ListView) findViewById(R.id.users);
		originalFriendList = new ArrayList<User>();
		workingFriendList = new ArrayList<User>();
		
		// listen for list item clicks
		friendView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				User selected = originalFriendList.get(position);
				toggleUserItem(view, !selected.selected);
				selected.toggle();
			}
			
		});
		
		// listen for the finish button click
		Button finish = (Button) findViewById(R.id.finish);
		finish.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for (User friend : originalFriendList) {
					if (friend.selected) {
						// @todo probably shouldn't be casting friend
						event.guests.add((Guest) friend);
					}
				}
				
				Intent intent = new Intent(getApplicationContext(), MyEventsActivity.class);
				intent.putExtra("data", event);
				startActivity(intent);
			}
			
		});
		
		
		// listen to search EditText changes
		search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				filter(s.toString());
				friendAdapter.notifyDataSetChanged();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
			
		});
		
		// get our friends from the server
		new GetFriends().execute();
	}
	
	/*
	 * Asynchronous background task that fetches a list of users from the API 
	 */
	private class GetFriends extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			// make call to API
			String str = API.getInstance().get("friends");

			if (str != null) {
				try {
					// parse result to a JSON Array
					JSONArray friends = new JSONArray(str);

					// loop through friends
					for (int i = 0; i < friends.length(); i++) {
						User user = new User(friends.getJSONObject(i));
						workingFriendList.add(user);
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
			
			originalFriendList = (ArrayList<User>) workingFriendList.clone();
			friendAdapter = new FriendAdapter(InviteFriendsActivity.this, R.id.users, workingFriendList);
			friendView.setAdapter(friendAdapter);
		}
		
	}
	
	/*
	 * Used to populate the ListView of friends
	 */
	private class FriendAdapter extends ArrayAdapter<User> {
		
		private ArrayList<User> friends;
	    private LayoutInflater inflater = null;
	    private ImageLoader imageLoader;
	    private DisplayImageOptions imageOptions;
	    
	    public FriendAdapter(Context context, int textViewResourceId, ArrayList<User> friends) {
			super(context, textViewResourceId, friends);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.friends = friends;
			imageLoader = ImageLoader.getInstance();
	        imageOptions = new DisplayImageOptions.Builder()
	        	.showImageOnLoading(R.drawable.blank_profile)
	        	.showImageForEmptyUri(R.drawable.blank_profile)
	        	.showImageOnFail(R.drawable.blank_profile)
	        	.cacheInMemory(true)
	        	.cacheOnDisc(true)
	        	.build();
		}
	 
	    // this method is called for every item in the listview
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	// create a new view if old view does not exist
	    	View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.user_item, null);
	        }
	        
	        User friend = friends.get(position);
	        
	        // set title
	        TextView name = (TextView) view.findViewById(R.id.name);
	        name.setText(friend.name);
	 
	        // set profile image
	        ImageAware photo = new ImageViewAware((ImageView) view.findViewById(R.id.photo), false);
	        imageLoader.displayImage(friend.photo, photo, imageOptions);
	        
	        // check to see if this should be checked
	        toggleUserItem(view, friend.selected);
	        
	        // set this view's tag to the entire data object
	        view.setTag(friend);
	        
	        return view;
	    }
	    
	}
	
	/*
	 * Adds styling to user_item relative layout
	 */
	public void toggleUserItem(View view, boolean toggle) {
		ImageView checked = (ImageView) view.findViewById(R.id.checkmark);
		if (toggle) {
			view.setBackgroundColor(getResources().getColor(R.color.green));
	    	checked.setVisibility(View.VISIBLE);
		} else {
			view.setBackgroundColor(0x00000000);
			checked.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_friends, menu);
		return true;
	}

}
