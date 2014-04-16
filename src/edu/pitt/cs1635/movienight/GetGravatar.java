package edu.pitt.cs1635.movienight;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class GetGravatar {
	
	public static String getGravatar(String email){
		String trim_email = email.toLowerCase().trim();
		String emailmd5 = "";
		try {
            
	        //Create MessageDigest object for MD5
	        MessageDigest digest = MessageDigest.getInstance("MD5");
	         
	        //Update input string in message digest
	        digest.update(trim_email.getBytes(), 0, trim_email.length());
	 
	        //Converts message digest value in base 16 (hex) 
	        emailmd5  = new BigInteger(1, digest.digest()).toString(16);
	 
	        } catch (NoSuchAlgorithmException e) {
	 
	            e.printStackTrace();
	        }
		
		String gravatarURL = "http://gravatar.com/avatar/"+emailmd5+".jpg?s=200";
		
		return gravatarURL;
	}

}
