package com.ht.RCSAndroidGUI;

public class Battery {
	private int level, scale, temperature, voltage;
	
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
