package com.android.dvci.db;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

public class RecordListVisitor extends RecordVisitor {

	public RecordListVisitor(String column) {
		this.projection = new String[] { column };
	}

	List<String> list = new ArrayList<String>();

	public List<String> getList() {
		return list;
	}

	@Override
	public long cursor(Cursor cursor) {
		String value = cursor.getString(0);
		list.add(value);
		return 0;
	}

}
