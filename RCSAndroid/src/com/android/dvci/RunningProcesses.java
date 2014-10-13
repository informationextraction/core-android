/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : RunningProcesses.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import com.android.dvci.auto.Cfg;
import com.android.dvci.gui.ASG;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;
import com.android.mm.M;

import java.util.List;

public class RunningProcesses {
	private static final String TAG = "RunningProcess"; //$NON-NLS-1$
	private static RunningProcesses instance;

	private String foreground = "";
	private final ActivityManager activityManager;

	private RunningProcesses() {
		activityManager = (ActivityManager) Status.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
	}

	public static RunningProcesses self() {
		if (instance == null) {
			instance = new RunningProcesses();
		}
		return instance;
	}

	public synchronized String getForeground() {
		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$

		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
		if (Cfg.DEBUG) {
			String newFore = taskInfo.get(0).topActivity.getPackageName();
			if (!foreground.equals(newFore)) {
				Check.log(TAG + " (update) topActivity CURRENT Activity: " + taskInfo.get(0).topActivity.getPackageName());
			}
		}

		if(taskInfo==null || taskInfo.isEmpty()){
			return "";
		}

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		foreground = componentInfo.getPackageName();
		return foreground;
	}

	public boolean isGuiVisible() {
		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$

		String pack = Status.self().getAppContext().getPackageName();

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
		for (ActivityManager.RunningTaskInfo ti: taskInfo){
			if(ti.topActivity.getPackageName().equals(pack))
				if (Cfg.DEBUG) {
					Check.log(TAG + " (isGuiVisible), found: " + pack);
				}
				return true;
		}

		return getForeground().equals(ASG.class);

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
