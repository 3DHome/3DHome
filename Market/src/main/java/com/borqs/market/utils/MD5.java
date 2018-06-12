package com.borqs.market.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * User: b608
 * Date: 12-9-28
 * Time: 下午5:28
 * To change this template use File | Settings | File Templates.
 */

public class MD5 {
	public static String toMd5Upper(byte[] bytes) {
		return toMd5(bytes).toUpperCase();
	} 
	
	public static String toMd5(byte[] bytes) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return toHexString(algorithm.digest(), "");
		} catch (NoSuchAlgorithmException e) {
			//Log.v("he--------------------------------ji", "toMd5(): " + e);
			throw new RuntimeException(e);
		}
	} 
	private static String toHexString(byte[] bytes, String separator) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			if (Integer.toHexString(0xFF & b).length() == 1)
				hexString.append("0").append(Integer.toHexString(0xFF & b));
	            else  
	            	hexString.append(Integer.toHexString(0xFF & b));
		}
		return hexString.toString();
	}
	
	public static String md5Base64(byte[] bytes) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return new String(Base64.encode(algorithm.digest(), Base64.NO_WRAP));
		} catch (NoSuchAlgorithmException e) {
			//Log.v("he--------------------------------ji", "toMd5(): " + e);
			throw new RuntimeException(e);
		}
	}
}
