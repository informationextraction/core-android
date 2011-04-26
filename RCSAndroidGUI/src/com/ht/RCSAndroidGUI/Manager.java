/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Manager.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class Manager.
 *
 * @param <T> the generic type
 */
public abstract class Manager<T extends ThreadBase, U> {
	/** The running. */
	protected  HashMap<U, T> running;
	
	/** The threads. */
	protected  HashMap<T, Thread> threads;
	
	/** The status. */
	protected Status status;
	
	/**
	 * Instantiates a new manager.
	 */
	public Manager(){
		status = Status.self();
		running = new HashMap<U, T>();
		threads = new HashMap<T, Thread>();
	}
	
	/**
	 * Start all.
	 *
	 * @return true, if successful
	 */
	public abstract boolean startAll();
	
	/**
	 * Stop all.
	 */
	public abstract void stopAll();
	
	/**
	 * Start.
	 *
	 * @param key the key
	 */
	public abstract  void start(U key);
	
	/**
	 * Stop.
	 *
	 * @param key the key
	 */
	public abstract  void stop(U key);
	
	/**
	 * Reload .
	 * 
	 * @param key
	 *            the key
	 */
	public final void reload(final U key) {
		Check.requires(running!=null, "Null running");
		final T a = running.get(key);
		if(a!=null){
			a.next();
		}
	}
	
	/**
	 * Restart .
	 * 
	 * @param key
	 *            the key
	 */
	public final synchronized void restart(final U key) {
		final T a = running.get(key);
		stop(key);
		start(key);
	}
	
	/**
	 * Gets the running.
	 *
	 * @return the running
	 */
	public HashMap<U, T> getRunning() {
		return running;
	}
}
