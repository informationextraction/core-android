package com.ht.RCSAndroidGUI;

import android.app.ActivityManager.RunningAppProcessInfo;

public class ProcessInfo {
	public RunningAppProcessInfo processInfo;
	public ProcessStatus status;
	
	public ProcessInfo(RunningAppProcessInfo processInfo, ProcessStatus status){
		this.processInfo=processInfo;
		this.status=status;
	}
}
