package com.ht.rcsandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class RCSAndroid.
 */
public class RCSAndroid extends Service {

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

		Toast.makeText(this, "Que - Service Created", Toast.LENGTH_LONG).show();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Que - Service Destroyed", Toast.LENGTH_LONG).show();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Toast.makeText(this, "Que - Service Started, starting thread", Toast.LENGTH_LONG).show();
		
		CoreThread core = new CoreThread();
		
		// Core starts
		core.Start();
		
		/*try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		Toast.makeText(this, "Que - Stopping thread", Toast.LENGTH_LONG).show();
		
		// Core stops
		core.Stop();
		core = null;*/
	}
}

