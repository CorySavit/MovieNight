package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;
import android.widget.TextView;

public class MovieDetailsActivity extends Activity {

	private ImageLoader imageLoader;
	private AlertDialog.Builder confirmBuilder;
	private Movie movie;
	private Theater myTheater;
	private Showtime myShowtime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_details);

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
		
		imageLoader = ImageLoader.getInstance();

		/*
		 * Set header data 
		 */

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
		DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.blank_poster)
			.showImageForEmptyUri(R.drawable.blank_poster)
			.showImageOnFail(R.drawable.blank_poster)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.build();
		ImageView poster = (ImageView) findViewById(R.id.poster);
		Bitmap bmp = null;
		if (movie.poster != null && movie.poster.length() > 0) {
			bmp = imageLoader.loadImageSync(movie.poster, imageOptions);

		} else {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.blank_poster);
		}
		bmp = StackBlur.blur(bmp, 10);
		poster.setImageBitmap(bmp);

		/*
		 * Setup tabs
		 */

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		final FrameLayout tabContent = tabHost.getTabContentView();
		
		// create tabs
		final String[] tabs = {"events", "theaters", "details", "ratings"};
		final int[] icons = {R.drawable.ic_action_android_calendar, R.drawable.ic_action_icon_ticket, R.drawable.ic_action_information, R.drawable.ic_action_icon_star};
		for (int i = 0; i < tabs.length; i++) {
			final View tabContentView = tabContent.getChildAt(i);
			TabSpec tabSpec = tabHost.newTabSpec(tabs[i]);
			tabSpec.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return tabContentView;
				}
			});
			tabSpec.setIndicator(null, getResources().getDrawable(icons[i]));
			tabHost.addTab(tabSpec);
		}

		// ensure that all but the first tab content are invisible
		for (int index = 1; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
		
		/*
		 * Populate tab content
		 */
		
		// populate feature events listview
		ListView events = (ListView) findViewById(R.id.events);
		events.setAdapter(new EventsAdapter(MovieDetailsActivity.this, R.layout.event_item, movie.events));
		
		// populate theater listview
		ListView theaters = (ListView) findViewById(R.id.theaters);
		theaters.setAdapter(new TheatersAdapter(MovieDetailsActivity.this, R.layout.theater_item, movie.theaters));
		
	}

	/*
	 * Used to populate the ListView of theaters (in a movie detail)
	 */
	private class TheatersAdapter extends ArrayAdapter<Theater> {

		private List<Theater> theaters;
		private LayoutInflater inflater = null;

		public TheatersAdapter(Context context, int layoutResourceId, List<Theater> data) {
			super(context, layoutResourceId, data);
			theaters = data;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	
	/*
	 * Used to populate the ListView of featured events
	 */
	private class EventsAdapter extends ArrayAdapter<Event> {
		private DisplayImageOptions imageOptions;
		private List<Event> events;
		private LayoutInflater inflater;

		public EventsAdapter(Context context, int layoutResourceId, List<Event> data) {
			super(context, layoutResourceId, data);
			events = data;
			imageOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.blank_profile)
				.showImageForEmptyUri(R.drawable.blank_profile)
				.showImageOnFail(R.drawable.blank_profile)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.build();
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// create a new view if old view does not exist
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.featured_event_item, parent, false);
			}

			// get appropriate event data
			Event event = events.get(position);

			// set tag (id)
			view.setTag(event);

			// set date
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(event.showtime + " on " + event.showtime.getDate());
			
			// set subtitle
			TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			int numGuest = event.guests.size();
			subtitle.setText("at " + event.theater + " with " + numGuest + " people");
			
			// set profile images
			// @todo too many profile photos seems to be added to the first event
			LinearLayout guests = (LinearLayout) view.findViewById(R.id.guests);
			int max = Math.min(numGuest, 6);
			for (int i = 0; i < max && guests.getChildCount() < max; i++) {
				FrameLayout myFrame = (FrameLayout) inflater.inflate(R.layout.profile_image, null);
				ImageAware photo = new ImageViewAware((ImageView) myFrame.findViewById(R.id.photo), false);
		        imageLoader.displayImage(event.guests.get(i).user.photo, photo, imageOptions);
				//TextView tv = (TextView) myFrame.findViewById(R.id.photo);
				//tv.setText(position + "--" + i);
		        guests.addView(myFrame);
			}

			return view;
		}

	}
	
	/*
	 * Used to populate the ListView of profile images for a given event
	 */
	private class ProfileImageAdapter extends ArrayAdapter<Event> {
		private int layoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions imageOptions;
		private List<Event> events;
		private LayoutInflater inflater;

		public ProfileImageAdapter(Context context, int layoutResourceId, List<Event> data, ImageLoader imageLoader, DisplayImageOptions imageOptions) {
			super(context, layoutResourceId, data);
			events = data;
			this.layoutResourceId = layoutResourceId;
			this.imageLoader = imageLoader;
			this.imageOptions = imageOptions;
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// create a new view if old view does not exist
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(layoutResourceId, parent, false);
			}

			// get appropriate event data
			Event event = events.get(position);

			// set tag (id)
			view.setTag(event);

			// set date
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(event.showtime + " on " + event.showtime.getDate());
			
			// set subtitle
			TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			int numGuest = event.guests.size();
			subtitle.setText("at " + event.theater + " with " + numGuest + " people");
			
			// set profile images
			LinearLayout guests = (LinearLayout) view.findViewById(R.id.guests);
			for (int i = 0; guests.getChildCount() < numGuest; i++) {
				FrameLayout myFrame = (FrameLayout) inflater.inflate(R.layout.profile_image, null);
				ImageAware photo = new ImageViewAware((ImageView) myFrame.findViewById(R.id.photo), false);
		        imageLoader.displayImage(event.guests.get(i).user.photo, photo, imageOptions);
		        guests.addView(myFrame);
			}

			return view;
		}

	}

}
