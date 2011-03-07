package com.ht.RCSAndroidGUI;

import android.util.Log;

public class Check {

	public static void asserts(boolean b, String string) {
		if (b != true) {
			Log.d("Que", "Asserts - " + string);
		}
	}
}
