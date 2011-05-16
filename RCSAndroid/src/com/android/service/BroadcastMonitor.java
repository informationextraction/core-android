package com.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.service.auto.Cfg;

/**
 * The Class BroadcastMonitor.
 */
public class BroadcastMonitor extends BroadcastReceiver {
	private static final String TAG = "BroadcastMonitor";
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "BroadcastMonitor Intent Received", Toast.LENGTH_LONG).show();
		if(Cfg.DEBUG) Log.d("QZ", TAG + " (onReceive): starting intent");
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.android.service.app");
		context.startService(serviceIntent);
	}
}