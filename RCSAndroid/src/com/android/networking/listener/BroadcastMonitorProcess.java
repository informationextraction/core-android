/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.listener;

import com.android.networking.RunningProcesses;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class BroadcastMonitorProcess extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess"; //$NON-NLS-1$

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
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
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
