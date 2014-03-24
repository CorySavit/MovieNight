package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class InviteFriendsActivity extends Activity {
	
	private Event event;
	private List<User> friendList;
	
	private ListView friendView;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_friends);
		
		// get intent data
		Intent intent = getIntent();
		event = (Event) intent.getSerializableExtra("data");
		
		// set listview
		friendView = (ListView) findViewById(R.id.users);
		friendList = new ArrayList<User>();
		
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
			String str = API.getInstance().get("user/friends");

			if (str != null) {
				try {
					// parse result to a JSON Array
					JSONArray friends = new JSONArray(str);

					// loop through friends
					for (int i = 0; i < friends.length(); i++) {
						friendList.add(new User(friends.getJSONObject(i)));
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
			
			// update the grid view with via our PosterAdapter
			ListAdapter adapter = new FriendAdapter(InviteFriendsActivity.this, friendList);
			friendView.setAdapter(adapter);
		}
		
	}
	
	/*
	 * Used to populate the ListView of friends
	 */
	private class FriendAdapter extends BaseAdapter {
		
		private Activity activity;
	    private List<User> friends;
	    private LayoutInflater inflater = null;
	    private ImageLoader imageLoader;
	    private DisplayImageOptions imageOptions;
	 
	    private FriendAdapter(Activity a, List<User> d) {
	        activity = a;
	        friends = d;
	        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        imageLoader = ImageLoader.getInstance();
	        imageOptions = new DisplayImageOptions.Builder()
	        	.showImageOnLoading(R.drawable.blank_profile)
	        	.showImageForEmptyUri(R.drawable.blank_profile)
	        	.showImageOnFail(R.drawable.blank_profile)
	        	.cacheInMemory(true)
	        	.cacheOnDisc(false)
	        	.build();
	    }
	    
	    @Override
		public int getCount() {
			return friends.size();
		}

		@Override
		// we have to implement this
		public Object getItem(int position) {
			return friends.get(position);
		}

		@Override
		// again, we have have to implement this
		public long getItemId(int position) {
			return position;
		}
	 
	    // this method is called for every item in the listview
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	// create a new view if old view does not exist
	    	View view = convertView;
	        if (convertView == null) {
	        	view = inflater.inflate(R.layout.user_item, null);
	        }
	        
	        // get appropriate movie data
	        User friend = friends.get(position);
	        
	        // set title
	        TextView name = (TextView) view.findViewById(R.id.name);
	        name.setText(friend.name);
	 
	        // set profile image
	        ImageAware photo = new ImageViewAware((ImageView) view.findViewById(R.id.photo), false);
	        imageLoader.displayImage(friend.photo, photo, imageOptions);
	        
	        // set this view's tag to the entire data object
	        view.setTag(friend);
	        
	        return view;
	    }
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_friends, menu);
		return true;
	}

}
