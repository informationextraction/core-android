package com.android.m;
import android.content.Context;
public class M {
	public static String e(String message){
		return message;
	}
	
	public static String d(String encoded, String otp){
		byte[] ebytes = encoded.getBytes();
		byte[] obytes = otp.getBytes();
		
		for (int i = 0; i < obytes.length; i++) {
			ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
		}
		return new String(ebytes);
	}

	public static void init(Context applicationContext) {
		// TODO Auto-generated method stub
		
	}
}
