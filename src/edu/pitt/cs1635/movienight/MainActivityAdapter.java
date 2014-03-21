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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class MainActivityAdapter extends BaseAdapter {
	
	private Activity activity;
    private List<Movie> movies;
    private static LayoutInflater inflater = null;
    private ImageLoader imageLoader;
    private DisplayImageOptions imageOptions;
 
    public MainActivityAdapter(Activity a, List<Movie> d) {
        activity = a;
        movies = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = ImageLoader.getInstance();
        imageOptions = new DisplayImageOptions.Builder()
        	.showImageOnLoading(R.drawable.blank_poster)
        	.showImageForEmptyUri(R.drawable.blank_poster)
        	.showImageOnFail(R.drawable.blank_poster)
        	.cacheInMemory(true)
        	.cacheOnDisc(false)
        	.build();
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
        // this ImageAware wrapper is supposed to solve the image re-load issue on scroll
        // see https://github.com/nostra13/Android-Universal-Image-Loader/issues/406
        ImageAware poster = new ImageViewAware((ImageView) view.findViewById(R.id.poster), false);
        
        // get appropriate movie data
        Movie movie = movies.get(position);
 
        // set all values in view
        title.setText(movie.title);
        imageLoader.displayImage(movie.poster, poster, imageOptions);
        
        // set this view's tag to the entire data object
        view.setTag(movie);
        
        return view;
    }
    
}
