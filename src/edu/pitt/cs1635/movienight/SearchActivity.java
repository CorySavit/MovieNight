package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends Activity {
	
	private ListView resultView;
	private EditText searchView;
	private List<User> users;
	private ResultAdapter resultAdapter;
	private SessionManager session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		session = new SessionManager(this);
		
		// listen for list item clicks
		resultView = (ListView) findViewById(R.id.result);
		resultView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), Profile.class);
				intent.putExtra(Profile.USER_ID, ((User) view.getTag()).id);
				startActivity(intent);
			}
			
		});
		
		// setup results adapter
		users = new ArrayList<User>();
		resultAdapter = new ResultAdapter(SearchActivity.this, users);
		resultView.setAdapter(resultAdapter);
		
		// listen to search EditText changes
		searchView = (EditText) findViewById(R.id.search);
		searchView.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// search for friends based on query
				new SearchFriends().execute(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
			
		});
		
		// populate listview will all users on initializaiton
		new SearchFriends().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
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
	
	private class SearchFriends extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... query) {
			
			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", Integer.toString(session.getId())));
			if (query.length > 0) {
				params.add(new BasicNameValuePair("q", query[0]));
			}
			String str = API.getInstance().get("user/search", params);

			if (str != null) {
				try {
					// clear current list
					users.clear();

					// add users to list
					JSONArray result = new JSONArray(str);
					for (int i = 0; i < result.length(); i++) {
						users.add(new User(result.getJSONObject(i)));
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
			
			// notify result adapter
			resultAdapter.notifyDataSetChanged();
		}
		
	}
	
	private class ResultAdapter extends BaseAdapter {
		
		private List<User> users;
	    private LayoutInflater inflater = null;
	    private ImageLoader imageLoader;
	    private DisplayImageOptions imageOptions;
	    
	    public ResultAdapter(Context context, List<User> list) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			users = list;
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
		public int getCount() {
			return users.size();
		}

		@Override
		public Object getItem(int position) {
			return users.get(position);
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
	        	view = inflater.inflate(R.layout.user_item, null);
	        }
	        
	        User user = users.get(position);
	        
	        // set title
	        TextView name = (TextView) view.findViewById(R.id.name);
	        name.setText(user.name);
	 
	        // set profile image
	        ImageAware photo = new ImageViewAware((ImageView) view.findViewById(R.id.photo), false);
	        imageLoader.displayImage(user.photo, photo, imageOptions);
	        
	        // set this view's tag to the entire data object
	        view.setTag(user);
	        
	        return view;
	    }
	    
	}

}
