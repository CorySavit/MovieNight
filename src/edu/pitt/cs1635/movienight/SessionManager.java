package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SessionManager {

	//private SessionManager instance = null;
	private LayoutInflater inflater;
	private final Context context;
	private SharedPreferences pref;
	private Editor editor;

	private AlertDialog loginDialog;
	private AlertDialog signupDialog;
	private AlertDialog loginSignupDialog;
	private ArrayList<NameValuePair> userdetails;

	private static final String PREF_NAME = "MovieNightPrefs";

	public static final String KEY_ID = "id";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_FIRSTNAME = "firstName";
	public static final String KEY_LASTNAME = "lastName";

	protected SessionManager(Context c) {
		this.context = c;
		inflater = LayoutInflater.from(context);
		pref = context.getSharedPreferences(PREF_NAME, 0);
		editor = pref.edit();

		/*
		 * Login/Signup Alert Dialog
		 */
     
		final View signInOutView = inflater.inflate(R.layout.dialog_signin_signout, null);

		loginSignupDialog = new AlertDialog.Builder(context)
			.setView(signInOutView)
			.create();

		((Button) signInOutView.findViewById(R.id.login_btn)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				loginSignupDialog.dismiss();
				showLoginDialog();
			}

		});

		((Button) signInOutView.findViewById(R.id.signup_btn)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				loginSignupDialog.dismiss();
				createSignupDialog();
			}

		});
		

		/*
		 * Login AlertDialog
		 */

		 final View loginView = inflater.inflate(R.layout.activity_signin, null);

		 loginDialog = new AlertDialog.Builder(context)
			 .setView(loginView)
			 .create();

		 ((Button) loginView.findViewById(R.id.login_btn)).setOnClickListener(new OnClickListener(){

			 @Override
			 public void onClick(View v) {
				 EditText emailInput = (EditText) loginView.findViewById(R.id.email_login);
				 EditText passwordInput = (EditText) loginView.findViewById(R.id.password_login);
				 ArrayList<NameValuePair> loginCreds = new ArrayList<NameValuePair>();
				 loginCreds.add(new BasicNameValuePair("email", emailInput.getText().toString()));
				 loginCreds.add(new BasicNameValuePair("password", passwordInput.getText().toString()));
				 new PostLogin().execute(loginCreds);
			 }

		 });

		 ((Button) loginView.findViewById(R.id.cancel_btn)).setOnClickListener(new OnClickListener(){

			 @Override
			 public void onClick(View v) {
				 loginDialog.dismiss();
			 }

		 });
		 

		 /*
		  * Signup AlertDialog
		  */

		 final View textEntryView = inflater.inflate(R.layout.activity_signup, null);

		 signupDialog = new AlertDialog.Builder(context)
			 .setView(textEntryView)
			 .create();

		 ((Button) textEntryView.findViewById(R.id.save_profile_btn)).setOnClickListener(new OnClickListener(){

			 @Override
			 public void onClick(View v) {
				 EditText f = (EditText) textEntryView.findViewById(R.id.signup_firstname);
				 EditText l = (EditText) textEntryView.findViewById(R.id.signup_lastname);
				 EditText e = (EditText) textEntryView.findViewById(R.id.signup_email);
				 EditText p1 = (EditText) textEntryView.findViewById(R.id.signup_password1);
				 EditText p2 = (EditText) textEntryView.findViewById(R.id.signup_password2);

				 String firstname = f.getText().toString();
				 String lastname = l.getText().toString();
				 String email = e.getText().toString();
				 String password1 = p1.getText().toString();
				 String password2 = p2.getText().toString();

				 //if "Password" and "Confirm Password" fields do not match,
				 //display error message via Toast and halt submition
				 if (!password1.equals(password2)){
					 Toast.makeText(context, "Passwords Do Not Match!", Toast.LENGTH_SHORT).show();
				 } else {
					 //otherwise add sign up information to userdetails to prep for POST
					 userdetails = new ArrayList<NameValuePair>();
					 userdetails.add(new BasicNameValuePair("first_name", firstname));
					 userdetails.add(new BasicNameValuePair("last_name", lastname));
					 userdetails.add(new BasicNameValuePair("email", email));
					 userdetails.add(new BasicNameValuePair("password", password1));
					 new PostSignup().execute(firstname, lastname, email, password1);
				 }
			 }
		 });
	}
	
	public void showLoginSignupDialog() {
		loginSignupDialog.show();
	}

	public void showLoginDialog() {           
		loginDialog.show();
	}
	
	public void createSignupDialog() {
		signupDialog.show();
	}

	private class PostLogin extends AsyncTask<ArrayList<NameValuePair>, Void, Boolean> {
		JSONObject login = null;

		@Override
		protected Boolean doInBackground(ArrayList<NameValuePair>... param) {
			Boolean loginTrue = false;

			// make call to API
			String str = API.getInstance().post("user/login", param[0]);

			if (str != null) {
				try {

					login = new JSONObject(str);
					if (login.getInt("login") == 1){
						loginTrue = true;
						editor.putInt(KEY_ID, login.getInt("id"));
						editor.putString(KEY_EMAIL, login.getString("email"));
						editor.putString(KEY_FIRSTNAME, login.getString("first_name"));
						editor.putString(KEY_LASTNAME, login.getString("last_name"));
						editor.commit();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return loginTrue;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			String message;
			if (result){
				loginDialog.dismiss();
				message = "Welcome back, " + getFirstName() + "!"; 
			} else {
				message = "Invalid email or password.";
			}
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}

	}

	private class PostSignup extends AsyncTask<String, Void, Boolean> {
		JSONObject login = null;
		Boolean signupTrue = false;
		@Override
		protected Boolean doInBackground(String... arg0) {

			// make call to API
			String str = API.getInstance().post("user", userdetails);

			if (str != null) {
				try {
					login = new JSONObject(str);
					signupTrue = true;
					editor.putString(KEY_FIRSTNAME, arg0[0]);
					editor.putString(KEY_LASTNAME, arg0[1]);
					editor.putString(KEY_EMAIL, arg0[2]);
					editor.putInt(KEY_ID, login.getInt("id"));
					editor.commit();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			} else {
				Log.e("ServiceHandler", "Failed to receive data from URL");
			}

			return signupTrue;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			String message;
			if (result){
				loginDialog.dismiss();
				message = "Welcome to MovieNight, " + getFirstName() + "!"; 
			} else {
				message = "Error signing up.";
			}
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}
	}
	
	//Login (after signup)
    public void createLoginSession(String firstName, String lastName, String email, String password, Integer id){
    	//probabably would be a good idea to connect with server and return the api key here.
        editor.putString(KEY_FIRSTNAME, firstName);
        editor.putString(KEY_LASTNAME, lastName);
        editor.putString(KEY_EMAIL, email);
        //editor.putString(KEY_PASS, password);
        editor.putInt(KEY_ID, id);
        editor.commit();
    }

	public void logout(){
		editor.remove(KEY_ID);
		editor.remove(KEY_EMAIL);
		editor.remove(KEY_FIRSTNAME);
		editor.remove(KEY_LASTNAME);
		editor.commit();
	}

	public HashMap<String, String> getUserDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// first name
		user.put(KEY_FIRSTNAME, pref.getString(KEY_FIRSTNAME, null));
		// last name
		user.put(KEY_LASTNAME, pref.getString(KEY_LASTNAME, null));
		// user email id
		user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

		return user;
	}

	public Boolean isLoggedIn(){
		return pref.contains(KEY_ID);
	}

	public int getId(){
		return pref.getInt(KEY_ID, -1);
	}

	public String getFirstName() {
		return pref.getString(KEY_FIRSTNAME, "");
	}

	public String getLastName() {
		return pref.getString(KEY_LASTNAME, "");
	}

	public String getName() {
		return getFirstName() + " " + getLastName();
	}

	public String getEmail() {
		return pref.getString(KEY_EMAIL, "");
	}

}

