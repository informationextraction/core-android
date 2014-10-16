package org.benews;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class PullIntentService extends Service {


	private BackgroundPuller core;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		core = BackgroundPuller.newCore(this);
		core.Start();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}
}
