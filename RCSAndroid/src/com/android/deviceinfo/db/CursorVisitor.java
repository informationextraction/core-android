package com.android.deviceinfo.db;

import android.database.Cursor;

public abstract class CursorVisitor {
	public abstract CursorVisitor factory(Cursor cursor);
}
