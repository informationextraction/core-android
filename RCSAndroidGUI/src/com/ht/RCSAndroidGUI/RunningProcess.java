package com.ht.RCSAndroidGUI;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.ht.RCSAndroidGUI.util.Utils;

public class RunningProcess {
	private static final String TAG = "RunningProcess";
	
	private ArrayList<ActivityManager.RunningAppProcessInfo> list;
	private ActivityManager activityManager;
	private long time;
	
	/** The singleton. */
	private volatile static RunningProcess singleton;
	
	/**
	 * Self.
	 * 
	 * @return the RunningProcess
	 */
	public static RunningProcess self() {
		if (singleton == null) {
			synchronized (RunningProcess.class) {
				if (singleton == null) {
					singleton = new RunningProcess();
				}
			}
		}

		return singleton;
	}
	
	private RunningProcess() {
		list = new ArrayList<ActivityManager.RunningAppProcessInfo>();
		list.clear();
		
		activityManager = (ActivityManager)Status.getAppContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
	}
	
	private void clear() {
		list.clear();
	}
	
	private void update() {
		if (activityManager == null)
			return;
		
		clear();
		
		list = (ArrayList<ActivityManager.RunningAppProcessInfo>)activityManager.getRunningAppProcesses();
		
		time = System.currentTimeMillis();
	}
	
	// DEBUG
	public void print() {
		if (list == null)
			return;
		
		Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();
		
		while (iter.hasNext()) {
			ActivityManager.RunningAppProcessInfo element = iter.next();

			Log.d("QZ", TAG + " (update): " + element.processName);
		}
	}
	
	public synchronized boolean isPresent(String p) {
		// Auto-aggiorniamo la lista al minimo ogni 3 sec
		if (System.currentTimeMillis() - time >= 3000) {
			time = System.currentTimeMillis();
			update();
		}

		Iterator<ActivityManager.RunningAppProcessInfo> iter = list.listIterator();
		
		if (list == null || list.size() == 0)
			return false;
		
		if (p.length() == 0)
			return false;
		
		while (iter.hasNext()) {
			ActivityManager.RunningAppProcessInfo element = iter.next();

			if (Utils.matchStar(p, element.processName) == true)
				return true;
		}
		
		return false;
	}
}
