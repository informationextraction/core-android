package com.android.dvci.module.chat;

public class FbConversation implements Conversation {

	protected String account;
	protected String id;
	protected Contact[] contacts;
	protected long timestamp;

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
