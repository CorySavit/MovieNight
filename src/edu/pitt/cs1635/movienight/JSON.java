package edu.pitt.cs1635.movienight;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Wrap default JSON methods with a null check
 */
public class JSON {
	
	/*
	 * getString
	 */
	
	public static String getString(JSONObject data, String key) {
		try {
			if (data.has(key) && !data.isNull(key) && data.length() > 0) {
				return data.getString(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getString(JSONArray data, int index) {
		try {
			if (!data.isNull(index) && data.getString(index).length() > 0) {
				return data.getString(index);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/*
	 * getInt
	 */
	
	public static int getInt(JSONObject data, String key) {
		try {
			if (data.has(key) && !data.isNull(key)) {
				return data.getInt(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/*
	 * getDouble
	 */
	
	public static double getDouble(JSONObject data, String key) {
		try {
			if (data.has(key) && !data.isNull(key)) {
				return data.getDouble(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	/*
	 * getJSONArray
	 */
	
	public static JSONArray getJSONArray(JSONObject data, String key) {
		try {
			if (data.has(key) && !data.isNull(key) && data.getJSONArray(key) instanceof JSONArray) {
				return data.getJSONArray(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/*
	 * getJSONObject
	 */
	
	public static JSONObject getJSONObject(JSONObject data, String key) {
		try {
			if (data.has(key) && !data.isNull(key) && data.getJSONObject(key) instanceof JSONObject) {
				return data.getJSONObject(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static JSONObject getJSONObject(JSONArray data, int key) {
		try {
			if (!data.isNull(key) && data.getJSONObject(key) instanceof JSONObject) {
				return data.getJSONObject(key);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
