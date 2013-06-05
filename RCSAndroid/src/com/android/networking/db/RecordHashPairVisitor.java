package com.android.networking.db;

import java.util.Hashtable;

import android.database.Cursor;

public class RecordHashPairVisitor extends RecordVisitor {

	Hashtable<String, String> map = new Hashtable<String, String>();

	public RecordHashPairVisitor(String key, String value) {
		this.projection = new String[] { key, value };
	}

	public Hashtable<String, String> getMap() {
		return map;
	}

	@Override
	public long cursor(Cursor cursor) {
		String key = cursor.getString(0);
		String value = cursor.getString(1);
		map.put(key, value);
		return 0;
	}
	
	public String get(String key){
		return map.get(key);
	}

	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

}
