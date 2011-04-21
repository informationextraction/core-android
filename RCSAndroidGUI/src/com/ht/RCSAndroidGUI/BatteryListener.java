package com.ht.RCSAndroidGUI;

import java.util.Iterator;

import com.ht.RCSAndroidGUI.event.BatteryEvent;

public class BatteryListener extends Listener {
	/** The Constant TAG. */
	private static final String TAG = "BatteryListener";
	
	@Override
	public void run(Object o) {
		if (observers.empty())
			return;
		
		Iterator<Object> iter = observers.iterator();
		 
		while (iter.hasNext()) {
			BatteryEvent evt = (BatteryEvent)iter.next();
			
			evt.batteryNotification((Battery)o);
		}
		
		return;
	}
}
