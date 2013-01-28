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
	public final long cursor(Cursor cursor) {

		String[] record = new String[getProjection().length];
		int rpos=0;
		for (String column : getProjection()) {
		
		  record[rpos] = cursor.getString(
				  cursor.getColumnIndex(column) );
		  		  		  
		  rpos++;
		}
		
		record(rpos, record);
		
		return rpos;
	}

}
