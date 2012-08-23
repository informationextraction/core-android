package com.android.networking.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.networking.Status;

public class GenericSqliteHelper  extends SQLiteOpenHelper{

	public GenericSqliteHelper( String name, int version) {
		super(Status.self().getAppContext(), name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
	}

	
}
