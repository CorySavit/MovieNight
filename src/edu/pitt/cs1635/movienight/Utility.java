package edu.pitt.cs1635.movienight;

import java.util.List;

public class Utility {

	/*
	 * Join a list of strings with a specified delimiter
	 */
	public static String join(List<String> list, String del) {
		String result = "";
		int size = list.size();
		for (int i = 0; i < size; i++) {
			result += list.get(i);
			if (i < size - 1) {
				result += del;
			}
		}
		return result;
	}

}
