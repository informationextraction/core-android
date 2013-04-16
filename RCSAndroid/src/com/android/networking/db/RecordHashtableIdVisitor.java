package com.android.networking.db;

import java.util.Hashtable;
import java.util.TreeMap;

import android.database.Cursor;

public class RecordHashtableIdVisitor extends RecordVisitor {

	TreeMap<String, Hashtable<String, String>> tree = new TreeMap<String, Hashtable<String,String>>();
	private String key;
	
	public RecordHashtableIdVisitor(String[] projection) {
		this.projection = projection;
		this.key = projection[0];
	}

	public Hashtable<String, String> getMap(String key){
		return tree.get(key);
	}
	
	@Override
	public long cursor(Cursor cursor) {
		Hashtable<String, String> map = new Hashtable<String, String>();
		int columnIndex = 0;
		
		for(String columnName: cursor.getColumnNames()){
			String value = cursor.getString(columnIndex);
			if(columnIndex==0){
				key = columnName;
			}
			map.put(columnName, value);
			columnIndex+=1;
		}
		tree.put(key, map);
		return 0;
	}

}
