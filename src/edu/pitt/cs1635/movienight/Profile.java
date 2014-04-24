package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.pitt.cs1635.movienight.SessionManager;

public class Profile extends Activity {
	
	private Menu menu;
	private SessionManager session;
	private String name;
	private String photo;
	private int userId;
	private Intent intent;
	private DisplayImageOptions imageOptions;
	private ImageLoader imageLoader;
	private TextView nameView;
	private ImageAware photoAware;
	private boolean friend;
	
	final static String USER_ID = "user_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_main);
		
		session = new SessionManager(getApplicationContext());
		
		imageLoader = ImageLoader.getInstance();
		imageOptions = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.blank_profile)
			.showImageForEmptyUri(R.drawable.blank_profile)
			.showImageOnFail(R.drawable.blank_profile)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.build();
		
		nameView = (TextView) findViewById(R.id.name);
        photoAware = new ImageViewAware((ImageView) findViewById(R.id.profile_image), false);
		
        // if user_id isn't passed in, just fallback on current user
        // @todo we probably don't need to do an API call for the current user; we could get from shared prefs
		intent = getIntent();
		userId = intent.getIntExtra(USER_ID, session.getId());
		new GetUserInfo().execute(userId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.profile, menu);
		
		if (userId == session.getId()) {
			// profile for the current user
			menu.findItem(R.id.action_logout).setVisible(true);
			menu.findItem(R.id.action_edit_profile).setVisible(true);
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_friend:
			new FriendUser().execute(true);
			showFriendMenuItem(false);
			return true;
		case R.id.action_unfriend:
			new FriendUser().execute(false);
			showFriendMenuItem(true);
			return true;
		case R.id.action_edit_profile:
			// @todo start activity for result?
			Intent intent = new Intent(getApplicationContext(), ProfileEdit.class);
			startActivity(intent);
			return true;
		case R.id.action_logout:
			session.logout();
	        finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void showFriendMenuItem(boolean bool) {
		MenuItem friend = menu.findItem(R.id.action_friend);
		MenuItem unfriend = menu.findItem(R.id.action_unfriend);
		
		if (bool) {
			// show follow menu item
			friend.setVisible(true);
			unfriend.setVisible(false);
		} else {
			// show unfollow menu item
			friend.setVisible(false);
			unfriend.setVisible(true);
		}
	}
	
	private class GetUserInfo extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... user) {

			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("friend_id", Integer.toString(session.getId())));
			String str = API.getInstance().get("user/" + user[0], params);

			if (str != null) {
				try {
					
					JSONObject result = new JSONObject(str);
					name = result.getString("first_name") + " " + result.getString("last_name");
					photo = result.getString("photo");
					friend = result.getInt("friend") == 1 ? true : false;
					
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
			nameView.setText(name);
			imageLoader.displayImage(photo, photoAware, imageOptions);
			
			if (session.isLoggedIn() && userId != session.getId()) {
				// if user is logged in and not on their own profile page
				showFriendMenuItem(!friend);
			}
		}

	}
	
	private class FriendUser extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected Void doInBackground(Boolean... bool) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			String friend = bool[0] ? "friend" : "unfriend";
			String str = API.getInstance().post("user/" + userId + "/" + friend, params);
			Log.d("TEST", str);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			showFriendMenuItem(friend);
			friend = friend ? false : true;
		}

	}

}
