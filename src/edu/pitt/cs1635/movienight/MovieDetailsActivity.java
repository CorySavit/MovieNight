package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MovieDetailsActivity extends Activity {
	
	private ImageLoader imageLoader;
	private DisplayImageOptions imageOptions;
	
	public String join(List<String> list, String del) {
		String result = "";
		int size = list.size();
		for (int i = 0; i < size; i++) {
			result += list.get(i);
			if (i < size - 1) {
				result += del;
			}
		}
		return result;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movie_details);
		
		imageLoader = ImageLoader.getInstance();

		// getting intent data
		Intent intent = getIntent();
		Movie movie = (Movie) intent.getSerializableExtra("data");

		// @todo use id to make an API call to get additional data?

		// set title
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(movie.title);
		
		// set subtitle
		TextView subtitleView = (TextView) findViewById(R.id.subtitle);
		List<String> subtitle = new ArrayList<String>();
		if (movie.rating != null) {
			subtitle.add(movie.rating);
		}
		if (movie.runtime != null) {
			subtitle.add(movie.runtime);
		}
		if (movie.genres != null && movie.genres.size() > 0) {
			subtitle.add(join(movie.genres, ", "));
		}
		subtitleView.setText(join(subtitle, " \u2014 "));
		
		// set blurred poster behind title
		ImageView poster = (ImageView) findViewById(R.id.poster);
		Bitmap bmp = null;
		if (movie.poster != null && movie.poster.length() > 0) {
			bmp = imageLoader.loadImageSync(movie.poster, imageOptions);
			
		} else {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.blank_poster);
		}
		bmp = StackBlur.blur(bmp, 10);
		poster.setImageBitmap(bmp);
	}

}
