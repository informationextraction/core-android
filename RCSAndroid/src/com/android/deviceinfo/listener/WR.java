package com.android.deviceinfo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class WR extends BroadcastReceiver {
	static final String TAG = "WifiReceiver";

	@Override
	public void onReceive(Context c, Intent intent) {	
		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()) == false && ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()) == false) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): not our intent: " + intent.getAction()); //$NON-NLS-1$
			}
			
			return;
		}

		ConnectivityManager cm = (ConnectivityManager)Status.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): ConnectivityManager is null"); //$NON-NLS-1$	
			}

			return;
		}

		NetworkInfo[] ni = cm.getAllNetworkInfo();

		Status.wifiConnected = false;
		Status.gsmConnected = false;

		if (ni == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): NetworkInfo is null"); //$NON-NLS-1$
			}

			return;
		}

		for (NetworkInfo network : ni) {
			if (network.getType() == ConnectivityManager.TYPE_WIFI) {
				Status.wifiConnected = network.isConnected();

				continue;
			}

			if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
				Status.gsmConnected = network.isConnected();

				continue;
			}
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onReceive): Wifi status: " + Status.wifiConnected + " GSM status: " + Status.gsmConnected); //$NON-NLS-1$
		}
	}
}
