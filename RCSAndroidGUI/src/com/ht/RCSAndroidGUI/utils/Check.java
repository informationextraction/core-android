package com.ht.RCSAndroidGUI.utils;

import android.util.Log;

public class Check {

	public static void asserts(boolean b, String string) {
		if (b != true) {
			Log.d("RCS", "Asserts - " + string);
		}
	}
}
