package com.android.networking.db;

import android.database.Cursor;

/**
 * Visitor that gets a cursor call for each record in the table
 * @author zeno
 *
 */
public abstract class RecordVisitor {

	public String table;
	public int count;
	public String[] projection=null;
	private String selection=null;

	public RecordVisitor(){};
	
	public RecordVisitor(String[] projection, String selection) {
		this.projection=projection;
		this.selection = selection;
	}

	public void init() {
	};

	public void close() {
	};

	//public void visitRecord(String[] fields) { };

	public abstract long cursor(Cursor cursor)  ;

	public final void init(String table,  int count) {
		this.table=table;
		this.count=count;
		init();
	}

	public String[] getProjection() {
		return projection;
	}

	public String getSelection() {
		return selection;
	}

	public boolean isStopRequested() {
		return false;
	}
}
