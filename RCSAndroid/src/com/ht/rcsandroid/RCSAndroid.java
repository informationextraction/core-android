package com.ht.rcsandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class RCSAndroid extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Toast.makeText(this, "Que - Service Created", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Que - Service Destroyed", Toast.LENGTH_LONG).show();
	}

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

