/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Path.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.file;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

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

	/** The hidden. */
	private static String hidden;

	// public static final String UPLOAD_DIR = "";

	private Path() {
	}

	/**
	 * Hidden.
	 * 
	 * @return the string
	 */
	public static String hidden() {
		return hidden;
	}

	public static String upload() {
		return hidden;
	}

	/**
	 * Make dirs.
	 * 
	 * @return true, if successful
	 */
	public static boolean makeDirs() {
		try {
			if (haveStorage()) {

				if(Cfg.DEBUG) Check.log( TAG + " (makeDirs): hidden = " + hidden());
				createDirectory(conf());
				createDirectory(markup());
				createDirectory(logs());
				
				if(Cfg.FILE){
					File file = new File(logs(),"logs.txt");
					file.delete();
				}

				return true;
			}
		} catch (final Exception e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: " + e.toString());
		}
		return false;
	}

	/**
	 * Check storage.
	 */
	public static boolean haveStorage() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageWriteable) {
			hidden = Environment.getExternalStorageDirectory() + "/" + ".LOST.FILES/";
			return true;
		} else {
			hidden = null;
			return false;
		}
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
	 * @param dir
	 *            the dir
	 * @return true, if successful
	 */
	public static boolean removeDirectory(final String dir) {
		final File file = new File(dir);
		return file.delete(); // TODO: anche su directory piene!
	}

	/**
	 * Creates the directory.
	 * 
	 * @param dir
	 *            the dir
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
		if (haveStorage()) {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long bytesAvailable = (long) stat.getBlockSize()
					* (long) stat.getBlockCount();
			long megAvailable = bytesAvailable / 1048576;
			if(Cfg.DEBUG) Check.log( TAG + " (freeSpace): " + megAvailable + " MiB");
			return bytesAvailable;
		} else {
			if(Cfg.DEBUG) Check.log( TAG + " (freeSpace) Error: no external path");
			return 0;
		}
	}

}
