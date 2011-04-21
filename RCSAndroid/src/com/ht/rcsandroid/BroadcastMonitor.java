package com.ht.rcsandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class BroadcastMonitor.
 */
public class BroadcastMonitor extends BroadcastReceiver {
	
	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Que - BroadcastMonitor Intent Received", Toast.LENGTH_LONG).show();
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.ht.RCSAndroid");
		context.startService(serviceIntent);
	}
}