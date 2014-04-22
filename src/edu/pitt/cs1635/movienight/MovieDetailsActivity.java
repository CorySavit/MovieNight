package edu.pitt.cs1635.movienight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MovieDetailsActivity extends Activity {

	private LayoutInflater inflater;
	private ImageLoader imageLoader;
	private AlertDialog.Builder confirmBuilder;
	private Movie movie;
	private Showtime myShowtime;
	private WebView ratingMeter;
	private SessionManager session;
	private DatePickerDialog datePickerDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_details);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		session = new SessionManager(this);

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
				intent.putExtra("data", new Event(myShowtime));

				// start invite friends activity
				startActivity(intent);
			}

		});

		// getting intent data
		Intent intent = getIntent();
		movie = (Movie) intent.getSerializableExtra("data");

		// set header content
		movie.setHeader(this);

		// this will be set when user makes a choice
		myShowtime = null;

		imageLoader = ImageLoader.getInstance();

		/*
		 * Setup tabs
		 */

		final TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {

				if (tabId == "ratings") {
					ratingMeter.loadUrl("javascript:fillMeter()");
				}

			}
		});

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
		// @todo unless featured events are empty
		for (int index = 1; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}

		// create date picker dialog
		Calendar c = Calendar.getInstance();
		final TextView changeDate = (TextView) findViewById(R.id.change_date);
		changeDate.setText("In theaters on " + formatDate(c.getTime()));
		
		datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
				changeDate.setText("In theaters on " + formatDate(selectedMonth, selectedDay, selectedYear));
				new UpdateShowtimes().execute(selectedYear + "-" + (selectedMonth+1) + "-" + selectedDay);
			}
			
		}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		
		changeDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				datePickerDialog.show();
			}

		});
		

		new GetMovieInfo().execute();

	}
	
	private String formatDate(int month, int day, int year) {
		return formatDate(day + "-" + (month + 1) + "-" + year);
	}
	
	private String formatDate(String str) {
		try {
			return formatDate((new SimpleDateFormat("d-M-yyyy", Locale.US)).parse(str));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String formatDate(Date date) {
		return (new SimpleDateFormat("MMMM d, yyyy", Locale.US)).format(date);
	}

	private LinearLayout createMovieDetail(String label) {
		LinearLayout result = (LinearLayout) inflater.inflate(R.layout.movie_info_item, null);
		((TextView) result.findViewById(R.id.label)).setText(label.toUpperCase());
		return result;
	}

	private static class TheaterViewHolder {
		TextView name;
		TextView address;
		LinearLayout showtimes;
	}

	/*
	 * Asynchronous background task that fetches movie details from the API 
	 */
	private class GetMovieInfo extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			// make call to API
			String str = API.getInstance().get("movies/" + movie.id);

			if (str != null) {
				try {
					movie = new Movie(new JSONObject(str));
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
					intent.putExtra("eventID", movie.events.get(position).id);
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
			TextView detailsOverview = new TextView(MovieDetailsActivity.this);
			detailsOverview.setText(movie.description);
			description.addView(detailsOverview);
			details.add(description);

			// create cast item
			// @todo make this nicer with images and whatnot
			LinearLayout cast = createMovieDetail(getString(R.string.cast_members));
			TextView castView = new TextView(MovieDetailsActivity.this);
			castView.setText(Utility.join(movie.cast, ", "));
			cast.addView(castView);
			details.add(cast);

			// add all items to movie details scrollview
			LinearLayout movieDetails = (LinearLayout) findViewById(R.id.details);
			for (LinearLayout item : details) {
				movieDetails.addView(item);
			}

			// ratings
			ratingMeter = (WebView) findViewById(R.id.rating_meter);
			ratingMeter.setBackgroundColor(0x00000000);
			ratingMeter.getSettings().setJavaScriptEnabled(true);
			ratingMeter.loadUrl(API.BASE_URL + "views/rating.php?movie_id=" + movie.id);

			LinearLayout externalRatings = (LinearLayout) findViewById(R.id.external_ratings);
			int[] ratings = {R.string.rotten_critic, R.string.rotten_audience, R.string.tmdb};
			for (int i = 0; i < ratings.length; i++) {
				LinearLayout rating = (LinearLayout) inflater.inflate(R.layout.rating_item, externalRatings, false);

				// set percentage
				TextView percent = (TextView) rating.findViewById(R.id.percent);
				switch (ratings[i]) {
				case R.string.rotten_critic:
					percent.setText(movie.rottenCritic + "%");
					break;
				case R.string.rotten_audience:
					percent.setText(movie.rottenAudience + "%");
					break;
				case R.string.tmdb:
					percent.setText((long) Math.round(movie.tmdbRating * 10) + "%");
					break;
				}

				// set label
				((TextView) rating.findViewById(R.id.label)).setText(getString(ratings[i]).toUpperCase());

				// add rating to parent view
				externalRatings.addView(rating);
			}
		}

	}
	
	private class UpdateShowtimes extends AsyncTask<String, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(String... date) {

			// make call to API
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("date", date[0]));
			String str = API.getInstance().get("movies/" + movie.id + "/showtimes", params);

			if (str != null) {
				try {
					return new JSONArray(str);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			super.onPostExecute(result);

			ListView theaters = (ListView) findViewById(R.id.theaters);
			movie.setTheaters(result);
			theaters.setAdapter(new TheatersAdapter(MovieDetailsActivity.this, movie.theaters));
		}

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
				viewHolder.address = (TextView) view.findViewById(R.id.address);
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

			// set address
			viewHolder.address.setText(theater.address);

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
						if (session.isLoggedIn()) {
							// @todo this is most likely not how you do this
							Theater myTheater = ((Theater) ((LinearLayout) v.getParent().getParent().getParent().getParent()).getTag(R.id.TAG_THEATER));
							myShowtime = (Showtime) v.getTag();
							myShowtime.movie = movie;
							myShowtime.theater = myTheater;
							Spanned message = Html.fromHtml("You are about to create a MovieNight for <b>" + movie.title + "</b> at <b>" + myTheater.name + "</b> on <b>" + myShowtime.getDate() + "</b> at <b>" + myShowtime + ".");
							confirmBuilder.setMessage(message).create().show();
						} else {
							session.showLoginSignupDialog();
						}
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
			viewHolder.subtitle.setText("at " + event.showtime.theater.name + " with " + numGuest + " people");

			// set profile images
			// TODO check to see if this is the best way to be doing this
			viewHolder.guests.removeAllViews();
			int max = Math.min(numGuest, 6);
			for (int i = 0; i < max; i++) {
				FrameLayout myFrame = (FrameLayout) inflater.inflate(R.layout.profile_image, null);
				ImageAware photo = new ImageViewAware((ImageView) myFrame.findViewById(R.id.photo), false);
				imageLoader.displayImage(event.guests.get(i).photo, photo, imageOptions);
				viewHolder.guests.addView(myFrame);
			}

			return view;
		}

	}

}
