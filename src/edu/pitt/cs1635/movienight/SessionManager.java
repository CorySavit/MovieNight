package edu.pitt.cs1635.movienight;

import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 
public class SessionManager {

    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
     
    private static final String PREF_NAME = "MovieNightPrefs";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_FIRSTNAME = "firstName";
    public static final String KEY_LASTNAME = "lastName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASS = "password";
    public static final String KEY_API = "api_key";
     
    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
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
    
    public void logout(){

        editor.putBoolean(IS_LOGIN, false);
        editor.putString(KEY_FIRSTNAME, "");
        editor.putString(KEY_LASTNAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.putString(KEY_PASS, "");
  
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

