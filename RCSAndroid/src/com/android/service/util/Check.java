/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Check.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.util;

import com.android.service.auto.AutoConfig;
import com.android.service.conf.Configuration;

import android.util.Log;

/**
 * The Class Check.
 */
public final class Check {

	private static final String TAG = "Check";
	private static boolean enabled;

	private Check() {
		enabled = AutoConfig.DEBUG;
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
		if (enabled && b != true) {
			Log.d("QZ", TAG + "##### Asserts - " + string + " #####");
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
			Log.d("QZ", TAG + "##### Requires - " + string + " #####");
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
			Log.d("QZ", TAG + "##### Ensures - " + string + " #####");
		}
	}
}
