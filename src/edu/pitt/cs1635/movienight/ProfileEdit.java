package edu.pitt.cs1635.movienight;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.pitt.cs1635.movienight.SessionManager;

public class ProfileEdit extends Activity {
	SessionManager session;
	HashMap<String, String> user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_edit);
		
		
		
		Button editButton = (Button) findViewById(R.id.save_profile_btn);
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				session = new SessionManager(getApplicationContext());
				
				
				EditText firstname = (EditText) findViewById(R.id.firstname_edit);
				EditText lastname = (EditText) findViewById(R.id.lastname_edit);
				EditText email = (EditText) findViewById(R.id.email_edit);
				
				session.createLoginSession(firstname.getText().toString(), lastname.getText().toString(), email.getText().toString(), "password", -1);
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
