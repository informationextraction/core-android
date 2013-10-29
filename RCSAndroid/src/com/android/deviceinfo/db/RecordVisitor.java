package com.android.deviceinfo.db;

import android.database.Cursor;

/**
 * Visitor that gets a cursor call for each record in the table
 * 
 * @author zeno
 * 
 */
public abstract class RecordVisitor {

	public String[] tables;
	public int count;
	public String[] projection = null;
	public String selection = null;
	private String order = null;

	public RecordVisitor() {
	};

	public RecordVisitor(String[] projection, String selection) {
		this.projection = projection;
		this.selection = selection;
	}

	public RecordVisitor(String[] projection, String selection, String order) {
		this.projection = projection;
		this.selection = selection;
		this.order = order;
	}

	public void init() {
	};

	public void close() {
	};

	// public void visitRecord(String[] fields) { };

	public abstract long cursor(Cursor cursor);

	public final void init(String[] tables, int count) {
		this.tables = tables;
		this.count = count;
		init();
	}

	public String[] getProjection() {
		return projection;
	}

	public String getSelection() {
		return selection;
	}

	public String getOrder() {
		return order;
	}

	public boolean isStopRequested() {
		return false;
	}
}
