package com.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class BroadcastMonitor.
 */
public class BroadcastMonitor extends BroadcastReceiver {
	private static final String TAG = "BroadcastMonitor"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context, "BroadcastMonitor Intent Received",
		// Toast.LENGTH_LONG).show();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onReceive): starting intent"); //$NON-NLS-1$
		}
		final Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.android.service.app");//Messages.getString("27.0")); //$NON-NLS-1$
		context.startService(serviceIntent);
	}
}