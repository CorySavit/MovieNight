package edu.pitt.cs1635.movienight;

public class API {
	private static API instance = null;
	private ServiceHandler sh;
	
	private static final String BASE_URL = "http://labs.amoscato.com/movienight-api/";
	
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
}
