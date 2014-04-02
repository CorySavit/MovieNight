package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MovieDetailsActivity extends Activity {

	private ImageLoader imageLoader;
	private DisplayImageOptions imageOptions;
	private AlertDialog.Builder confirmBuilder;
	private Movie movie;
	private Theater myTheater;
	private Showtime myShowtime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_details);

		imageLoader = ImageLoader.getInstance();
		
		// create alert dialog
		confirmBuilder = new AlertDialog.Builder(MovieDetailsActivity.this)
			.setTitle(R.string.event_summary)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.create_event, new DialogInterface.OnClickListener() {

				@Override
				// called when user select 'Create Event'
				public void onClick(DialogInterface dialog, int which) {
					
					Intent intent = new Intent(getApplicationContext(), InviteFriendsActivity.class);
					
					// keep track of what the user has selected
					intent.putExtra("data", new Event(movie, myTheater, myShowtime));
					
					// start invite friends activity
					startActivity(intent);
				}
				
			});

		// getting intent data
		Intent intent = getIntent();
		movie = (Movie) intent.getSerializableExtra("data");
		
		// these will be set when user makes a choice
		myTheater = null;
		myShowtime = null;

		// set title
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(movie.title);

		// set subtitle
		TextView subtitle1View = (TextView) findViewById(R.id.subtitle);
		List<String> subtitle = new ArrayList<String>();
		if (movie.rating != null) {
			subtitle.add(movie.rating);
		}
		if (movie.runtime != null) {
			subtitle.add(movie.runtime);
		}
		String subtitleText = (Utility.join(subtitle, " \u2014 "));
		
		if (movie.genres != null && movie.genres.size() > 0) {
			subtitleText += "\n" + Utility.join(movie.genres, ", ");
		}
		subtitle1View.setText(subtitleText);
		

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

			// create a new view if old view does not exist
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.theater_item, parent, false);
			}

			// get appropriate theater data
			Theater theater = theaters.get(position);
			
			// set tag (id)
			view.setTag(theater);

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
				Showtime time = theater.showtimes.get(i); 
				tv.setText(time.toString());
				tv.setTag(time);
				showtimes.addView(layout);
				
				// @todo check to see if we should not be doing this for each textview
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// @todo this is most likely not how you do this
						myTheater = ((Theater) ((LinearLayout) v.getParent().getParent().getParent().getParent()).getTag());
						myShowtime = (Showtime) v.getTag();
						Spanned message = Html.fromHtml("You are about to create a MovieNight for <b>" + movie.title + "</b> at <b>" + myTheater.name + "</b> on <b>" + myShowtime.getDate() + "</b> at <b>" + myShowtime + ".");
						confirmBuilder.setMessage(message).create().show();
					}
					
				});
			}

			return view;
		}

	}

}
