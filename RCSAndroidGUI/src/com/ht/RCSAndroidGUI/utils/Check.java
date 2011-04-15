/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Check.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.utils;

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class Check.
 */
public class Check {

	/**
	 * Asserts.
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void asserts(final boolean b, final String string) {
		if (b != true) {
			Log.d("RCS", "Asserts - " + string);
		}
	}

	/**
	 * Requires.
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void requires(final boolean b, final String string) {
		if (b != true) {
			Log.d("RCS", "Requires - " + string);
		}
	}

	/**
	 * Ensures.
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void ensures(final boolean b, final String string) {
		if (b != true) {
			Log.d("RCS", "Ensures - " + string);
		}
	}
}
