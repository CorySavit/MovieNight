package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.ImageLoader;

public class LazyAdapter extends BaseAdapter {
	
	private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; 
 
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = ImageLoader.getInstance();
    }
    
    @Override
	public int getCount() {
		return data.size();
	}

	@Override
	// we have to implement this
	public Object getItem(int position) {
		return position;
	}

	@Override
	// again, we have have to implement this
	public long getItemId(int position) {

		return position;
	}
 
    // see http://developer.android.com/reference/android/widget/Adapter.html#getView(int, android.view.View, android.view.ViewGroup)
    // this method is called for every grid in the gridview
    public View getView(int position, View convertView, ViewGroup parent) {
        
    	// create a new view if old view does not exist
    	View view = convertView;
        if (convertView == null) {
        	view = inflater.inflate(R.layout.movie_poster, null);
        }
        
        // get our layout elements
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView poster = (ImageView) view.findViewById(R.id.poster);
        
        // get appropriate movie data
        HashMap<String, String> movie = new HashMap<String, String>();
        movie = data.get(position);
 
        // set all values in view
        title.setText(movie.get(MainActivity.TAG_TITLE));
        imageLoader.displayImage(movie.get(MainActivity.TAG_POSTER), poster);
        
        // set this view's tag to the unique movie id
        view.setTag(movie.get(MainActivity.TAG_ID));
        
        return view;
    }
    
}
