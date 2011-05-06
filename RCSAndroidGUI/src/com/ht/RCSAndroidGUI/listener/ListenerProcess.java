/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : ListenerProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

import com.ht.RCSAndroidGUI.ProcessInfo;
import com.ht.RCSAndroidGUI.ProcessStatus;
import com.ht.RCSAndroidGUI.RunningProcesses;

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
	
	protected int dispatch(RunningProcesses processes){
		
		ArrayList<RunningAppProcessInfo> list = processes.getProcessList();
		if (list == null) {
			return 0;
		}
		currentRunning.clear();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			RunningAppProcessInfo running = (RunningAppProcessInfo) iterator.next();
			if (running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

				currentRunning.put(running.processName, running);
				if (!lastRunning.containsKey(running.processName)) {
					Log.d("QZ", TAG + " (notification): started " + running.processName);
					dispatch(new ProcessInfo(running, ProcessStatus.START));
				} else {
					lastRunning.remove(running.processName);
				}
			}
		}

		for (Iterator iter = lastRunning.keySet().iterator(); iter.hasNext();) {
			RunningAppProcessInfo norun = (RunningAppProcessInfo) lastRunning.get(iter.next());
			Log.d("QZ", TAG + " (notification): stopped " + norun.processName);
			super.dispatch(new ProcessInfo(norun, ProcessStatus.STOP));
		}

		lastRunning = new TreeMap<String, RunningAppProcessInfo>(currentRunning);

		return 0;
	}

}
