package com.android.networking.util;

import android.os.Debug;

public class AntiDebug {
	public boolean DebugCheckIp(){
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
	
	public boolean DebugCheckConnected(){
		return Debug.isDebuggerConnected();
	}
}
