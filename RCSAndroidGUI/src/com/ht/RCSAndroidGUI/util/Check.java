/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Check.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.util;

import android.util.Log;

/**
 * The Class Check.
 */
public final class Check {

	private static final String TAG = "Check";

	private Check(){
	}
	/**
	 * Asserts, used to verify the truth of an expression
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void asserts(final boolean b, final String string) {
		if (b != true) {
			Log.d(TAG, "Asserts - " + string);
		}
	}

	/**
	 * Requires. Used to check prerequisites of a method.
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void requires(final boolean b, final String string) {
		if (b != true) {
			Log.d(TAG, "Requires - " + string);
		}
	}

	/**
	 * Ensures. Checks to be done at the end of a method.
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void ensures(final boolean b, final String string) {
		if (b != true) {
			Log.d(TAG, "Ensures - " + string);
		}
	}
}
