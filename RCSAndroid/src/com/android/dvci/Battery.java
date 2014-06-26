/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Battery.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci;

public class Battery {
	private final int level, scale, temperature, voltage;

	public Battery(int l, int s, int t, int v) {
		level = l;
		scale = s;
		temperature = t;
		voltage = t;
	}

	public int getBatteryLevel() {
		return level;
	}

	public int getBatteryScale() {
		return scale;
	}

	public int getBatteryTemperature() {
		return temperature;
	}

	public int getBatteryVoltage() {
		return voltage;
	}
}
