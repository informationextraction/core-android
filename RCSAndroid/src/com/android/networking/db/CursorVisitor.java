package com.android.networking.db;

import android.database.Cursor;

public abstract class CursorVisitor {
	public abstract CursorVisitor factory(Cursor cursor);
}
