package com.android.dvci.module.chat;

public class HangoutConversation implements Conversation {

	protected String account;
	protected String id;
	protected long date;
	protected boolean group;
	protected String remote;

	public boolean isGroup() {
		return group;
	}

}
