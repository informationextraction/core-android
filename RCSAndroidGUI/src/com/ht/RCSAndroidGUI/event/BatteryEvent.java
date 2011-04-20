package com.ht.RCSAndroidGUI.event;

import android.util.Log;

import com.ht.RCSAndroidGUI.Battery;
import com.ht.RCSAndroidGUI.Status;

public class BatteryEvent extends EventBase {
	private Status statusObj;
	
	@Override
	public void begin() {
		statusObj = Status.self();
		
		statusObj.attachToBattery(this);
	}

	@Override
	public void end() {
		statusObj.detachFromBattery(this);
	}

	@Override
	public void parse(EventConf event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
		
	}

	public void batteryNotification(Battery b) {
		Log.d("QZ", "Got battery notification: " + b.getBatteryLevel() + "%");
	}
}
