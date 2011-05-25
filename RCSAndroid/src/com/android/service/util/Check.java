/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : if(Cfg.DEBUG) Check.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.util;

import java.io.IOException;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.file.AutoFile;
import com.android.service.file.Path;

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
			if(Cfg.DEBUG) Check.log( TAG + "##### Asserts - " + string + " #####");
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
			if(Cfg.DEBUG) Check.log( TAG + "##### Requires - " + string + " #####");
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
			if(Cfg.DEBUG) Check.log( TAG + "##### Ensures - " + string + " #####");
		}
	}

	public static void log(String string) {
		if(Cfg.DEBUG) {
			Log.d("QZ", string);
			if(Cfg.FILE){
				AutoFile file = new AutoFile(Path.logs(),"logs.txt");
				file.append(string + "\n");
			}
		}
	}

	public static void log(Throwable e) {
		if(Cfg.DEBUG) {
			e.printStackTrace();
			log("Exception: " + e.toString());
		}
		
	}
}
