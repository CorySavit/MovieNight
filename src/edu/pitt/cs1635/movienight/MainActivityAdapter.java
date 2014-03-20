package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivityAdapter extends BaseAdapter {
	
	private Activity activity;
    private List<Movie> movies;
    private static LayoutInflater inflater = null;
    public ImageLoader imageLoader; 
 
    public MainActivityAdapter(Activity a, List<Movie> d) {
        activity = a;
        movies = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = ImageLoader.getInstance();
    }
    
    @Override
	public int getCount() {
		return movies.size();
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
        Movie movie = movies.get(position);
 
        // set all values in view
        title.setText(movie.title);
        imageLoader.displayImage(movie.poster, poster);
        
        // set this view's tag to the entire data object
        view.setTag(movie);
        
        return view;
    }
    
}
