package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

		// populate theater listview
		ListView theaters = (ListView) findViewById(R.id.theaters);
		theaters.setAdapter(new TheatersAdapter(MovieDetailsActivity.this, movie.theaters));
	}

	/*
	 * Used to populate the ListView of theaters (in a movie detail)
	 */
	private class TheatersAdapter extends BaseAdapter {

		private Activity activity;
		private List<Theater> theaters;
		private LayoutInflater inflater = null;

		public TheatersAdapter(Activity a, List<Theater> d) {
			activity = a;
			theaters = d;
			inflater = activity.getLayoutInflater();
		}

		@Override
		public int getCount() {
			return theaters.size();
		}

		@Override
		public Object getItem(int position) {
			return theaters.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			//System.out.println(position);

			// create a new view if old view does not exist
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.theater_item, parent, false);
			}

			// get appropriate theater data
			Theater theater = theaters.get(position);

			// set name
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(theater.name);

			// set showtimes
			LinearLayout showtimes = (LinearLayout) view.findViewById(R.id.showtimes);
			int size = theater.showtimes.size();
			// @todo check to see whether or not showtimes.getChildCount() is a hack...
			for (int i = 0; i < size && showtimes.getChildCount() < size; i++) {
				LinearLayout layout = (LinearLayout) View.inflate(MovieDetailsActivity.this, R.layout.showtime_button, null);
				TextView tv = (TextView) layout.findViewById(R.id.time);
				tv.setText(theater.showtimes.get(i).toString());
				showtimes.addView(layout);
			}

			return view;
		}

	}

}
