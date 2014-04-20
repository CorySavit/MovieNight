package edu.pitt.cs1635.movienight;

import java.util.List;

import org.apache.http.NameValuePair;

public class API {
	private static API instance = null;
	private ServiceHandler sh;
	
	static final String BASE_URL = "http://labs.amoscato.com/movienight-api/";
	
	// protected constructor defeats instantiation
	protected API() {
		sh = new ServiceHandler();
	}
	
	// returns single instance of API or create one if it doesn't exist
	public static API getInstance() {
		if (instance == null) {
			instance = new API();
			
		}
		return instance;
	}
	
	// gets something from the API
	public String get(String method) {
		return sh.makeServiceCall(BASE_URL + method, ServiceHandler.GET);
	}
	
	public String get(String method, List<NameValuePair> params) {
		return sh.makeServiceCall(BASE_URL + method, ServiceHandler.GET, params); 
	}
	
	public String post(String method) {
		return sh.makeServiceCall(BASE_URL + method, ServiceHandler.POST);
	}
	
	public String post(String method, List<NameValuePair> params) {
		return sh.makeServiceCall(BASE_URL + method, ServiceHandler.POST, params); 
	}
	
	public String put(String method, List<NameValuePair> params) {
		return sh.makeServiceCall(BASE_URL + method, ServiceHandler.PUT, params); 
	}
}
