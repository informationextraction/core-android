package com.android.deviceinfo.listener;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// http://marakana.com/s/post/1291/android_device_policy_administration_tutorial
public class AR extends android.app.admin.DeviceAdminReceiver {
	static final String TAG = "DemoDeviceAdminReceiver";

	/** Called when this application is approved to be a device administrator. */
	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onEnabled) ");
		}
		
		Status.self().setDeviceAdmin(true);
	}

	/** Called when this application is no longer the device administrator. */
	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onDisabled) ");
		}
		Status.self().setDeviceAdmin(false);
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		super.onPasswordChanged(context, intent);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onPasswordChanged) ");
		}
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onPasswordFailed) ");
		}
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		super.onPasswordSucceeded(context, intent);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onPasswordSucceeded) ");
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Detect admin disable requests
		if (intent.getAction().equals(ACTION_DEVICE_ADMIN_DISABLE_REQUESTED)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onReceive): admin disabling requested"); //$NON-NLS-1$
			}
		} else {
			super.onReceive(context, intent);
		}      
	}
}