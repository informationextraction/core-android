package com.android.networking.db;

import android.database.Cursor;

/**
 * Visitor that gets a cursor call for each record in the table
 * @author zeno
 *
 */
public abstract class RecordVisitor {

	public String table;
	public String[] columns;
	public int count;
	public String[] projection=null;

	public void init() {
	};

	public void close() {
	};

	//public void visitRecord(String[] fields) { };

	public abstract long cursor(Cursor cursor)  ;

	public final void init(String table, String[] columns, int count) {
		this.table=table;
		this.columns = columns;
		this.count=count;
		init();
	}

	public String[] getProjection() {
		return projection;
	}

	public String getSelection() {
		return null;
	}

	public boolean isStopRequested() {
		return false;
	}
}
