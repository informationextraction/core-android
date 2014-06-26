package com.android.dvci.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

/**
 * Visitor that gets a record call for each record in the table
 * 
 * @author zeno
 * 
 */
public class RecordStringVisitor extends RecordVisitor {

	// public abstract void record(int rpos, String[] record);
	List<String> records = new ArrayList<String>();

	public RecordStringVisitor(String column) {
		this.projection = new String[]{column};
	}
	
	public List<String> getRecords() {
		return records;
	}

	@Override
	public final void init() {
		records = new ArrayList<String>();
	}

	@Override
	public final void close() {

	}

	@Override
	public final long cursor(Cursor cursor) {

		String record = cursor.getString(0);
		records.add(record);

		return 0;
	}

}
