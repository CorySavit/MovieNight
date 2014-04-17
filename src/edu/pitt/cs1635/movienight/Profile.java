package edu.pitt.cs1635.movienight;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import edu.pitt.cs1635.movienight.SessionManager;
import edu.pitt.cs1635.movienight.GetGravatar;

public class Profile extends Activity {
	SessionManager session;
	HashMap<String, String> user;
	String emailmd5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_main);
		session = new SessionManager(getApplicationContext());
		user = session.getUserDetails();
		
		String firstName = user.get(SessionManager.KEY_FIRSTNAME);
		String lastName = user.get(SessionManager.KEY_LASTNAME);
		String email = user.get(SessionManager.KEY_EMAIL);
		TextView username = (TextView) findViewById(R.id.username);
		TextView user_email = (TextView) findViewById(R.id.user_email);
		
		username.setText(firstName+' '+lastName);
		user_email.setText(email);
		
		
		
			String imageURL = GetGravatar.getGravatar(email);
			ImageLoader imageLoader = ImageLoader.getInstance();
			DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.build();
			
			// set profile image
	        ImageAware photo = new ImageViewAware((ImageView) findViewById(R.id.profile_image), false);
	        imageLoader.displayImage(imageURL, photo, imageOptions);
			
			
		
		
		
		Button editButton = (Button) findViewById(R.id.edit_profile_btn);
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ProfileEdit.class);
				startActivity(intent);
				
			}
			
		});
		
		Button logoutButton = (Button) findViewById(R.id.logout_btn);
		logoutButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				session = new SessionManager(getApplicationContext());
				session.logout();
				Intent intent = getIntent();
		         finish();
			}
			
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

}
