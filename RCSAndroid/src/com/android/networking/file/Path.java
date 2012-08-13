/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Path.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.file;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Path.
 */
public class Path {

	/** The Constant TAG. */
	private static final String TAG = "PATH"; //$NON-NLS-1$

	/** The Constant CONF_DIR. */
	private static String CONF_DIR; //$NON-NLS-1$
	// public static final String DEBUG_DIR = "dwm/";
	/** The Constant MARKUP_DIR. */
	private static String MARKUP_DIR; //$NON-NLS-1$

	/** The Constant LOG_DIR. */
	private static String LOG_DIR; //$NON-NLS-1$

	private static String curLogFile;

	public static final String LOG_FILE = "logs"; //$NON-NLS-1$

	/** The hidden. */
	private static String hidden;

	private static boolean initialized = false;

	// public static final String UPLOAD_DIR = "";

	private Path() {

	}

	/**
	 * Make dirs.
	 * 
	 * @return true, if successful
	 */
	public static boolean makeDirs() {
		/** The Constant CONF_DIR. */
		CONF_DIR = Messages.getString("24.0"); //$NON-NLS-1$
		// public static final String DEBUG_DIR = "dwm/";
		/** The Constant MARKUP_DIR. */
		MARKUP_DIR = Messages.getString("24.1"); //$NON-NLS-1$

		/** The Constant LOG_DIR. */
		LOG_DIR = Messages.getString("24.2"); //$NON-NLS-1$

		try {
			if (haveStorage()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (makeDirs): hidden = " + hidden());//$NON-NLS-1$
				}

				boolean success = true;

				success &= createDirectory(conf());
				success &= createDirectory(markup());
				success &= createDirectory(logs());

				if (Cfg.FILE) {
					DateTime dt = new DateTime();

					curLogFile = LOG_FILE + "-" + dt.getOrderedString() + ".txt";

					final File file = new File(logs(), curLogFile);

					file.createNewFile();
				}

				initialized = success;
				return success;
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + e.toString());//$NON-NLS-1$
			}
		}

		return false;
	}

	/**
	 * Check.storage. //$NON-NLS-1$
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
			hidden = Environment.getExternalStorageDirectory() + Messages.getString("24.5"); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		} else {
			hidden = null;
			return false;
		}
	}

	public static String home() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
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

	public static String getCurLogfile() {
		return curLogFile;
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
			final StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			final long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			final long megAvailable = bytesAvailable / 1048576;
			if (Cfg.DEBUG) {
				Check.log(TAG + " (freeSpace): " + megAvailable + " MiB");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return bytesAvailable;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (freeSpace) Error: no external path");//$NON-NLS-1$
			}
			return 0;
		}
	}

	public static boolean initialized() {
		return initialized;
	}

}
