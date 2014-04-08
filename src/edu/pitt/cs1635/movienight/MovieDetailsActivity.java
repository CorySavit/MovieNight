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
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MovieDetailsActivity extends Activity {

	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private AlertDialog.Builder confirmBuilder;
	private Movie movie;
	private Theater myTheater;
	private Showtime myShowtime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_details);
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

		// set header content
		movie.setHeader(this);

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
		events.setAdapter(new EventsAdapter(MovieDetailsActivity.this, movie.events));
		events.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
				intent.putExtra("movie", movie);
				intent.putExtra("event", movie.events.get(position));
				startActivity(intent);
			}
			
		});
		
		// populate theater listview
		ListView theaters = (ListView) findViewById(R.id.theaters);
		theaters.setAdapter(new TheatersAdapter(MovieDetailsActivity.this, movie.theaters));
		
		// populate movie details information
		List<LinearLayout> details = new ArrayList<LinearLayout>();
		
		// create description item
		LinearLayout description = createMovieDetail(getString(R.string.overview));
		TextView detailsOverview = new TextView(this);
		detailsOverview.setText(movie.description);
		description.addView(detailsOverview);
		details.add(description);
		
		// create cast item
		// @todo make this nicer with images and whatnot
		LinearLayout cast = createMovieDetail(getString(R.string.cast_members));
		TextView castView = new TextView(this);
		castView.setText(Utility.join(movie.cast, ", "));
		cast.addView(castView);
		details.add(cast);
		
		// add all items to movie details scrollview
		LinearLayout movieDetails = (LinearLayout) findViewById(R.id.details);
		for (LinearLayout item : details) {
			movieDetails.addView(item);
		}
		
	}
	
	private LinearLayout createMovieDetail(String label) {
		LinearLayout result = (LinearLayout) inflater.inflate(R.layout.movie_info_item, null);
		((TextView) result.findViewById(R.id.label)).setText(label.toUpperCase());
		return result;
	}
	
	private static class TheaterViewHolder {
		TextView name;
		LinearLayout showtimes;
	}

	/*
	 * Used to populate the ListView of theaters (in a movie detail)
	 */
	private class TheatersAdapter extends BaseAdapter {
		
		private TheaterViewHolder viewHolder;
		private List<Theater> theaters;
		private LayoutInflater inflater = null;

		public TheatersAdapter(Activity activity, List<Theater> data) {
			theaters = data;
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				
				// limit calls to findViewById()
				// see http://www.piwai.info/android-adapter-good-practices/#ViewHolder-Pattern
				viewHolder = new TheaterViewHolder();
				viewHolder.name = (TextView) view.findViewById(R.id.name);
				viewHolder.showtimes = (LinearLayout) view.findViewById(R.id.showtimes);
				
				view.setTag(R.id.TAG_VIEW_HOLDER, viewHolder);
			} else {
				viewHolder = (TheaterViewHolder) view.getTag(R.id.TAG_VIEW_HOLDER);
			}

			// get appropriate theater data
			Theater theater = theaters.get(position);
			view.setTag(R.id.TAG_THEATER, theater);

			// set name
			viewHolder.name.setText(theater.name);

			// set showtimes
			viewHolder.showtimes.removeAllViews();
			int size = theater.showtimes.size();
			for (int i = 0; i < size; i++) {
				LinearLayout layout = (LinearLayout) View.inflate(MovieDetailsActivity.this, R.layout.showtime_button, null);
				TextView tv = (TextView) layout.findViewById(R.id.time);
				Showtime time = theater.showtimes.get(i); 
				tv.setText(time.toString());
				tv.setTag(time);
				viewHolder.showtimes.addView(layout);

				// @todo check to see if we should not be doing this for each textview
				tv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// @todo this is most likely not how you do this
						myTheater = ((Theater) ((LinearLayout) v.getParent().getParent().getParent().getParent()).getTag(R.id.TAG_THEATER));
						myShowtime = (Showtime) v.getTag();
						Spanned message = Html.fromHtml("You are about to create a MovieNight for <b>" + movie.title + "</b> at <b>" + myTheater.name + "</b> on <b>" + myShowtime.getDate() + "</b> at <b>" + myShowtime + ".");
						confirmBuilder.setMessage(message).create().show();
					}

				});
			}

			return view;
		}

	}
	
	private static class EventViewHolder {
		TextView title;
		TextView subtitle;
		LinearLayout guests;
	}
	
	/*
	 * Used to populate the ListView of featured events
	 */
	private class EventsAdapter extends BaseAdapter {
		
		private EventViewHolder viewHolder;
		private DisplayImageOptions imageOptions;
		private List<Event> events;
		private LayoutInflater inflater;

		public EventsAdapter(Activity activity, List<Event> data) {
			events = data;
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			return events.size();
		}

		@Override
		public Object getItem(int position) {
			return events.get(position);
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
				view = inflater.inflate(R.layout.featured_event_item, parent, false);
				
				viewHolder = new EventViewHolder();
				viewHolder.title = (TextView) view.findViewById(R.id.title);
				viewHolder.subtitle = (TextView) view.findViewById(R.id.subtitle);
				viewHolder.guests = (LinearLayout) view.findViewById(R.id.guests);
				
				view.setTag(R.id.TAG_VIEW_HOLDER, viewHolder);
			} else {
				viewHolder = (EventViewHolder) view.getTag(R.id.TAG_VIEW_HOLDER);
			}

			// get appropriate event data
			Event event = events.get(position);
			view.setTag(R.id.TAG_EVENT, event);

			// set date
			viewHolder.title.setText(event.showtime + " on " + event.showtime.getDate());
			
			// set subtitle
			int numGuest = event.guests.size();
			viewHolder.subtitle.setText("at " + event.theater + " with " + numGuest + " people");
			
			// set profile images
			// TODO check to see if this is the best way to be doing this
			viewHolder.guests.removeAllViews();
			int max = Math.min(numGuest, 6);
			for (int i = 0; i < max; i++) {
				FrameLayout myFrame = (FrameLayout) inflater.inflate(R.layout.profile_image, null);
				ImageAware photo = new ImageViewAware((ImageView) myFrame.findViewById(R.id.photo), false);
		        imageLoader.displayImage(event.guests.get(i).user.photo, photo, imageOptions);
				viewHolder.guests.addView(myFrame);
			}

			return view;
		}

	}

}
