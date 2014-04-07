package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

public class Movie implements Serializable {
	private static final long serialVersionUID = 1L;

	// JSON keys
	static final String ID = "id";
	static final String TMS_ID = "tmsid";
	static final String TMDB_ID = "tmdbid";
	static final String IMDB_ID = "imdbid";
	
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String GENRES = "genres";
	static final String RATING = "rating";
	static final String RUNTIME = "runtime";
	static final String POSTER = "poster";
	static final String BACKDROP = "backdrop";
	static final String CAST = "cast";
	static final String MN_RATING = "mn_rating";
	
	static final String THEATERS = "theaters";
	static final String EVENTS = "events";

	// object variables
	String id; // @todo these ids should probably be ints
	String tmsid;
	int tmdbid;
	String imdbid;
	
	String title;
	String description;
	List<Genre> genres;
	String rating;
	String runtime;
	String poster;
	String backdrop;
	List<CastMember> cast;
	int mnRating;
	
	List<Theater> theaters;
	List<Event> events;

	public Movie(JSONObject data) {

		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getString(data, ID);
		tmsid = JSON.getString(data, TMS_ID);
		tmdbid = JSON.getInt(data, TMDB_ID);
		imdbid = JSON.getString(data, IMDB_ID);
		
		title = JSON.getString(data, TITLE);
		description = JSON.getString(data, DESCRIPTION);
		rating = JSON.getString(data, RATING);
		runtime = JSON.getString(data, RUNTIME);
		poster = JSON.getString(data, POSTER);
		backdrop = JSON.getString(data, BACKDROP);
		mnRating = JSON.getInt(data, MN_RATING);

		genres = new ArrayList<Genre>();
		JSONArray myGenres = JSON.getJSONArray(data, GENRES);
		if (myGenres != null) {
			for (int i = 0; i < myGenres.length(); i++) {
				genres.add(new Genre(JSON.getJSONObject(myGenres, i)));
			}
		}
		
		/*
		cast = new ArrayList<CastMember>();
		JSONArray myCast = JSON.getJSONArray(data, CAST);
		if (myCast != null) {
			for (int i = 0; i < myCast.length(); i++) {
				cast.add(new CastMember(JSON.getJSONObject(myCast, i)));
			}
		}
		*/

		theaters = new ArrayList<Theater>();
		JSONObject myTheaters = JSON.getJSONObject(data, THEATERS);
		if (myTheaters != null) {
			Iterator<?> keys = myTheaters.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				theaters.add(new Theater(JSON.getJSONObject(myTheaters, key)));
			}
		}
		
		events = new ArrayList<Event>();
		JSONArray myEvents = JSON.getJSONArray(data, EVENTS);
		if (myEvents != null) {
			for (int i = 0; i < myEvents.length(); i++) {
				events.add(new Event(JSON.getJSONObject(myEvents, i)));
			}
		}
		
	}
	
	/**
	 * Populates movie_heading.xml based on this movie's data
	 */
	public void setHeader(Activity activity) {
		// set title
		TextView titleView = (TextView) activity.findViewById(R.id.title);
		titleView.setText(title);

		// set subtitle
		TextView subtitle1View = (TextView) activity.findViewById(R.id.subtitle);
		List<String> subtitle = new ArrayList<String>();
		if (rating != null) {
			subtitle.add(rating);
		}
		if (runtime != null) {
			subtitle.add(runtime);
		}
		String subtitleText = (Utility.join(subtitle, " \u2014 "));

		if (genres != null && genres.size() > 0) {
			subtitleText += "\n" + Utility.join(genres, ", ");
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
		ImageView poster = (ImageView) activity.findViewById(R.id.poster);
		Bitmap bmp = null;
		if (this.poster != null && !this.poster.isEmpty()) {
			bmp = ImageLoader.getInstance().loadImageSync(this.poster, imageOptions);
		} else {
			bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.blank_poster);
		}
		bmp = StackBlur.blur(bmp, 10);
		poster.setImageBitmap(bmp);
	}
}
