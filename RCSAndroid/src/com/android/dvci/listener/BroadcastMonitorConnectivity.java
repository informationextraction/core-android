/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorConnectivity.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.listener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.dvci.Connectivity;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

// Falso broadcast, e' generato da noi
public class BroadcastMonitorConnectivity extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorConnectivity"; //$NON-NLS-1$

	private boolean stop;
	private final int period;

	public BroadcastMonitorConnectivity() {
		stop = false;
		period = 60000; // Poll interval
		
		if (Cfg.DEBUG) {
			setName(getClass().getSimpleName());
		}
	}

	@Override
	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			final ConnectivityManager connectivityManager = (ConnectivityManager) Status.getAppContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			if (connectivityManager == null) {
				return;
			}

			final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

			if (activeNetworkInfo != null) {
				onReceive(activeNetworkInfo.isConnected());
			} else {
				onReceive(false);
			}

			try {
				wait(period);
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

	public void onReceive(boolean isConnected) {
		ListenerConnectivity.self().dispatch(new Connectivity(isConnected));
	}

	void register() {
		stop = false;
	}

	synchronized void unregister() {
		stop = true;
		notify();
	}
}
