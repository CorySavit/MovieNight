package edu.pitt.cs1635.movienight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

public class EventDetailsActivity extends Activity {
	
	private Event event;
	private Movie movie;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_details);
		
		// getting intent data
		Intent intent = getIntent();
		movie = (Movie) intent.getSerializableExtra("movie");
		event = (Event) intent.getSerializableExtra("event");
		
		// set header content
		movie.setHeader(this);
		
		/*
		 * Setup tabs
		 */

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		final FrameLayout tabContent = tabHost.getTabContentView();
		
		// create tabs
		final String[] tabs = {"details", "guests"};
		for (int i = 0; i < tabs.length; i++) {
			final View tabContentView = tabContent.getChildAt(i);
			TabSpec tabSpec = tabHost.newTabSpec(tabs[i]);
			tabSpec.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return tabContentView;
				}
			});
			tabSpec.setIndicator(getString(getResources().getIdentifier(tabs[i], "string", "edu.pitt.cs1635.movienight")));
			tabHost.addTab(tabSpec);
		}

		// ensure that all but the first tab content are invisible
		for (int index = 1; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
