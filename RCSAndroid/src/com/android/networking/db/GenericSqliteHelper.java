package com.android.networking.db;

import com.android.networking.Status;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

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
