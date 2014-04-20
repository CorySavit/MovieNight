package edu.pitt.cs1635.movienight;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    AlertDialog loginDialog;
    AlertDialog.Builder alert;
    private static SessionManager instance = null;
     
    private static final String PREF_NAME = "MovieNightPrefs";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_LASTNAME = "lastName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASS = "password";
    public static final String KEY_API = "api_key";
     
    // Constructor
    protected SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    public static SessionManager getInstance(Context context){
    	if (instance == null){
    		instance = new SessionManager(context);
    	}
    	return instance;
    }
    
    /*******************
     * Login functions *
     *******************/
    
    //Login (after signup)
    public void createLoginSession(String firstName, String lastName, String email, String password, String id){
    	//probabably would be a good idea to connect with server and return the api key here.
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_FIRSTNAME, firstName);
        editor.putString(KEY_LASTNAME, lastName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASS, password);
        editor.putString(KEY_API, id);
        editor.commit();
    }
    
    //Login (Regular)
    public void login(String email, String password){
    	ArrayList<NameValuePair> loginCreds = new ArrayList<NameValuePair>();
    	loginCreds.add(new BasicNameValuePair("email", email));
    	loginCreds.add(new BasicNameValuePair("password", password));
    	new PostLogin().execute(loginCreds);
    	
    }
    
    public AlertDialog loginDialog(Context context) {
  		Log.d("Made it to LoginDialog", "True");
  	    LayoutInflater factory = LayoutInflater.from(context);           
  	    final View textEntryView = factory.inflate(R.layout.activity_signin, null);
  	    
  	    alert = new AlertDialog.Builder(context);
  	    alert.setView(textEntryView);
  	    Button cancel = (Button) textEntryView.findViewById(R.id.cancel_btn);
  	    Button loginSubmit = (Button) textEntryView.findViewById(R.id.login_btn);
  	    loginDialog = alert.create();
  	    
  	    loginSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				final EditText emailInput = (EditText) textEntryView.findViewById(R.id.email_login);
				final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.password_login);
	            login(emailInput.getText().toString(), passwordInput.getText().toString());
			}
  	    	
  	    });
  	    
  	  cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				loginDialog.dismiss();
			}
	    	
	    });
  	    

  	    
  	    /*alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
  	        public void onClick(DialogInterface dialog, int whichButton) {
  	                final EditText emailInput = (EditText) textEntryView.findViewById(R.id.email_login);
  	                final EditText passwordInput = (EditText) textEntryView.findViewById(R.id.password_login);
  	                login(emailInput.getText().toString(), passwordInput.getText().toString());
  	        }
  	    });
  	    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
  	        public void onClick(DialogInterface dialog, int whichButton) {
  	        }
  	    });*/
  	    return loginDialog;
  	}
      
      private class PostLogin extends AsyncTask<ArrayList<NameValuePair>, Void, Boolean> {
  		JSONObject login = null;
  		@Override
  		protected Boolean doInBackground(ArrayList<NameValuePair>... arg0) {
  			Boolean loginTrue = false;
  			
  			// make call to API
  			String str = API.getInstance().post("user/login", arg0[0]);

  			if (str != null) {
  				try {
  					login = new JSONObject(str);
  	
  					Integer loginSuccess =login.getInt("login");

  					if(loginSuccess==1){
  						loginTrue = true;
  						editor.putBoolean(IS_LOGIN, true);
  						editor.putString(KEY_FIRSTNAME, login.getString("first_name"));
  						editor.putString(KEY_LASTNAME, login.getString("last_name"));
  						editor.putString(KEY_EMAIL, login.getString("email"));
  						editor.putString(KEY_API, "id");
  						editor.commit();
  					}
  				} catch (JSONException e) {
  					// TODO Auto-generated catch block
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
  			if (result){
  				CharSequence text = "You are now logged in!";
  	    		int duration = Toast.LENGTH_SHORT;
      			Toast toast = Toast.makeText(_context, text, duration);
      			toast.show();
      			loginDialog.dismiss();
  			}
  			else if (!result){
  	    		CharSequence text = "Login Failure!";
  	    		int duration = Toast.LENGTH_SHORT;
      			Toast toast = Toast.makeText(_context, text, duration);
      			toast.show();
  			}

  		}

  	}
    
    public void logout(){

        editor.putBoolean(IS_LOGIN, false);
        editor.putString(KEY_FIRSTNAME, "");
        editor.putString(KEY_LASTNAME, "");
        editor.putString(KEY_EMAIL, "");  
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
    	return pref.getBoolean(IS_LOGIN, false);
    }
    
    public int getUserKey(){
    	return pref.getInt(KEY_API, 0);
    }
    
  
    
}

