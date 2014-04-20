package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends Activity {
	SessionManager session;
	List<NameValuePair> userdetails;
	
	String firstname;
	String lastname;
	String email;
	String password1;
	String password2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		
		//grab Submit button and set on click listener
		Button editButton = (Button) findViewById(R.id.save_profile_btn);
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//create new SessionManager instance (used for SharedPreferences
				session = new SessionManager(getApplicationContext());
				//grab all edit text fields
				EditText f = (EditText) findViewById(R.id.signup_firstname);
				EditText l = (EditText) findViewById(R.id.signup_lastname);
				EditText e = (EditText) findViewById(R.id.signup_email);
				EditText p1 = (EditText) findViewById(R.id.signup_password1);
				EditText p2 = (EditText) findViewById(R.id.signup_password2);
				
				firstname = f.getText().toString();
				lastname = l.getText().toString();
				email = e.getText().toString();
				password1 = p1.getText().toString();
				password2 = p2.getText().toString();
				Log.d("pass1", p1.getText().toString());
				Log.d("pass2", p2.getText().toString());
				
				
				
				//if "Password" and "Confirm Password" fields do not match,
				//display error message via Toast and halt submition
				if (!password1.equals(password2)){
					Context context = getApplicationContext();
		    		CharSequence text = "Passwords Do Not Match!";
		    		int duration = Toast.LENGTH_SHORT;

	    			Toast toast = Toast.makeText(context, text, duration);
	    			toast.show();
				} else {
					//otherwise add sign up information to userdetails to prep for POST
					userdetails = new ArrayList<NameValuePair>();
					userdetails.add(new BasicNameValuePair("first_name", firstname));
					userdetails.add(new BasicNameValuePair("last_name", lastname));
					userdetails.add(new BasicNameValuePair("email", email));
					userdetails.add(new BasicNameValuePair("password", password1));
							
					new PostSignup().execute();
				}
				
				
				
			}
			
		});
	}
	
	private class PostSignup extends AsyncTask<Void, Void, Void> {
		JSONObject login = null;
		@Override
		protected Void doInBackground(Void... arg0) {
			
			// make call to API
			String str = API.getInstance().post("user", userdetails);

			if (str != null) {
				try {
					login = new JSONObject(str);
					Log.d("ID", login.getString("id")) ;
					Log.d("Success?", login.getString("success"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
			if(login != null ){
				try {
					Integer id = login.getInt("id");
					session.createLoginSession(firstname, lastname, email, password1, id);
					Intent profile = new Intent(getApplicationContext(), Profile.class);
					finish();
					startActivity(profile);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

}