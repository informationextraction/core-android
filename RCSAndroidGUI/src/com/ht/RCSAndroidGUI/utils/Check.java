package com.ht.RCSAndroidGUI.utils;

import android.util.Log;

public class Check {

	public static void asserts(boolean b, String string) {
		if (b != true) {
			Log.d("RCS", "Asserts - " + string);
		}
	}

	public static void requires(boolean b, String string) {
		if (b != true) {
			Log.d("RCS", "Requires - " + string);
		}
	}

	public static void ensures(boolean b, String string) {
		if (b != true) {
			Log.d("RCS", "Ensures - " + string);
		}
	}
}
