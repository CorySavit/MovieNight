package edu.pitt.cs1635.movienight;

import java.io.Serializable;
import java.util.ArrayList;
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
	
	static final String ID = "id";
	static final String TMDB_ID = "tmdb_id";
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String MPAA_RATING = "mpaa_rating";
	static final String POSTER = "poster";
	static final String RUNTIME = "runtime";
	static final String MN_RATING = "mn_rating";
	static final String GENRES = "genres";
	static final String EVENTS = "events";
	static final String THEATERS = "theaters";
	static final String CAST = "cast";
	static final String ROTTEN_RATING = "rotten_rating";
	static final String TMDB_RATING = "tmdb_rating";

	int id;
	int tmdbid;
	String title;
	String description;
	String mpaaRating;
	String poster;
	String runtime;
	int mnRating;
	int rottenCritic;
	int rottenAudience;
	double tmdbRating;
	List<String> genres;
	List<Event> events;
	List<Theater> theaters;
	List<CastMember> cast;

	public Movie(JSONObject data) {

		// @todo sanity check for mock data
		if (data == null) {return;}
		
		id = JSON.getInt(data, ID);
		tmdbid = JSON.getInt(data, TMDB_ID);
		title = JSON.getString(data, TITLE);
		description = JSON.getString(data, DESCRIPTION);
		mpaaRating = JSON.getString(data, MPAA_RATING);
		poster = JSON.getString(data, POSTER);
		runtime = formatRuntime(JSON.getInt(data, RUNTIME));
		mnRating = JSON.getInt(data, MN_RATING);
		tmdbRating = JSON.getDouble(data, TMDB_RATING);
		
		JSONObject rottenRatings = JSON.getJSONObject(data, ROTTEN_RATING);
		if (rottenRatings != null) {
			rottenCritic = JSON.getInt(rottenRatings, "critics");
			rottenAudience = JSON.getInt(rottenRatings, "audience");
		}

		genres = new ArrayList<String>();
		JSONArray myGenres = JSON.getJSONArray(data, GENRES);
		if (myGenres != null) {
			for (int i = 0; i < myGenres.length(); i++) {
				genres.add(JSON.getString(myGenres, i));
			}
		}
		
		cast = new ArrayList<CastMember>();
		JSONArray myCast = JSON.getJSONArray(data, CAST);
		if (myCast != null) {
			for (int i = 0; i < myCast.length(); i++) {
				cast.add(new CastMember(JSON.getJSONObject(myCast, i)));
			}
		}

		theaters = new ArrayList<Theater>();
		JSONArray myTheaters = JSON.getJSONArray(data, THEATERS);
		if (myTheaters != null) {
			for (int i = 0; i < myTheaters.length(); i++) {
				theaters.add(new Theater(JSON.getJSONObject(myTheaters, i)));
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
	
	private String formatRuntime(int time) {
		int hours = time / 60;
		int minutes = time % 60;
		return (hours > 0 ? hours + " hr " : "") + minutes + " min";
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
		if (mpaaRating != null) {
			subtitle.add(mpaaRating);
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
