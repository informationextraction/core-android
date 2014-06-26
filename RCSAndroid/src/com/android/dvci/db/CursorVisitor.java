package com.android.dvci.db;

import android.database.Cursor;

public abstract class CursorVisitor {
	public abstract CursorVisitor factory(Cursor cursor);
}
