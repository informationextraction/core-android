package com.ht.RCSAndroidGUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class AcListener extends Listener<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "AcListener";

	private BroadcastReceiver acOnReceiver, acOffReceiver;

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
		registerAcOn();
		registerAcOff();
	}

	@Override
	protected void stop() {
		Status.getAppContext().unregisterReceiver(acOnReceiver);
		Status.getAppContext().unregisterReceiver(acOffReceiver);
	}
	
	/**
	 * Register Power Connected.
	 */
	private void registerAcOn() {
		acOnReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(final Context context, final Intent intent) {		
				Log.d("QZ", TAG + " registerAcOn");
				
				// Called every time we have a change of status
				int plugged = intent.getIntExtra("plugged", 0);
				
				dispatch(new Ac(plugged == 1));
			}
		};
		
		final IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		
		// Register the broadcastreceiver and filter it to only get power status changes
		Status.getAppContext().registerReceiver(acOnReceiver, filter);
	}
	
	/**
	 * Register Power Disconnected.
	 */
	private void registerAcOff() {
		BroadcastReceiver acOffReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(final Context context, final Intent intent) {		
				Log.d("QZ", TAG + " registerAcOff");
				
				// Called every time we have a change of status
				int plugged = intent.getIntExtra("plugged", 0);
				
				dispatch(new Ac(plugged == 0));
			}
		};
		
		final IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		
		// Register the broadcastreceiver and filter it to only get power status changes
		Status.getAppContext().registerReceiver(acOffReceiver, filter);
	}
}
