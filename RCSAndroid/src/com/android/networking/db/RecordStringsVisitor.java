package com.android.networking.db;

import android.database.Cursor;

/**
 *  Visitor that gets a record call for each record in the table
 * @author zeno
 *
 */
public abstract class RecordStringsVisitor extends RecordVisitor {

	public abstract void record(int rpos, String[] record);
	
	@Override
	public final int cursor(Cursor cursor) {

		String[] record = new String[columns.length];
		int rpos=0;
		for (String column : columns) {
		
		  record[rpos] = cursor.getString(
				  cursor.getColumnIndex(column) );
		  		  
		  record(rpos, record);
		  rpos++;
		}
		
		return rpos;
	}

}
