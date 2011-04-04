/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.util.HashMap;

import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.conf.Option;

// Singleton Class
public class Status {
	private volatile static Status singleton;

	private HashMap<Integer, Agent> agentsMap;
	private HashMap<Integer, Event> eventsMap;
	private HashMap<Integer, Action> actionsMap;
	private HashMap<Integer, Option> optionsMap;
	
	private Status() {
		agentsMap = new HashMap<Integer, Agent>();
		eventsMap = new HashMap<Integer, Event>();
		actionsMap = new HashMap<Integer, Action>();
		optionsMap = new HashMap<Integer, Option>();
	}
	
	public static Status self() {
		if (singleton == null) {
			synchronized(Status.class) {
				if (singleton == null) {
                    singleton = new Status();
                }
			}
		}

		return singleton;
	}
	
	public void clean() {
		agentsMap.clear();
		eventsMap.clear();
		actionsMap.clear();
		optionsMap.clear();
	}
	
	// Add an agent to the map
	public void addAgent(Agent a) throws RCSException {
		// Don't add the same agent twice
		if (agentsMap.containsKey(a.getId()) == true) {
			throw new RCSException("Agent " + a.getId() + " already loaded");			
		}
		
		agentsMap.put(a.getId(), a);
	}
	
	// Stop an agent
	public void stopAgent(Agent a) throws RCSException {
		if (agentsMap.containsKey(a.getId()) == false) {
			throw new RCSException("Agent " + a.getId() + " cannot be stopped because it doesn't exist");			
		}
		
		Agent agent = agentsMap.get(a.getId());
		
		if (agent == null)
			return;
		
		agent.stopAgent();
	}
	
	// Add an event to the map
	public void addEvent(Event e) throws RCSException {
		// Don't add the same event twice
		if (eventsMap.containsKey(e.getId()) == true) {
			throw new RCSException("Event " + e.getId() + " already loaded");			
		}
		
		eventsMap.put(e.getId(), e);
	}
	
	// Add an action to the map
	public void addAction(Action a) throws RCSException {
		// Don't add the same action twice
		if (actionsMap.containsKey(a.getId()) == true) {
			throw new RCSException("Action " + a.getId() + " already loaded");			
		}
		
		actionsMap.put(a.getId(), a);
	}
	
	// Add an option to the map
	public void addOption(Option o) throws RCSException {
		// Don't add the same option twice
		if (optionsMap.containsKey(o.getId()) == true) {
			throw new RCSException("Option " + o.getId() + " already loaded");			
		}
		
		optionsMap.put(o.getId(), o);
	}
	
	public int getActionsNumber() {
		return actionsMap.size();
	}
	
	public int getAgentsNumber() {
		return agentsMap.size();
	}

	public int getEventsNumber() {
		return eventsMap.size();
	}

	public int getOptionssNumber() {
		return optionsMap.size();
	}
	
	public HashMap<Integer, Agent> getAgentsMap() {
		return agentsMap;
	}
	
	public HashMap<Integer, Event> getEventsMap() {
		return eventsMap;
	}
	
	public HashMap<Integer, Action> getActionsMap() {
		return actionsMap;
	}

	public Action getAction(int index) throws RCSException {
		if (actionsMap.containsKey(index) == false) {
			throw new RCSException("Action " + index + " not found");			
		}
		
		Action a = actionsMap.get(index);
		
		if (a == null) {
			throw new RCSException("Action " + index + " is null");
		}
		
		return a;
	}
	
	public Agent getAgent(int id) throws RCSException {
		if (agentsMap.containsKey(id) == false) {
			throw new RCSException("Agent " + id + " not found");			
		}
		
		Agent a = agentsMap.get(id);
		
		if (a == null) {
			throw new RCSException("Agent " + id + " is null");
		}
		
		return a;
	}
	
	public Event getEvent(int id) throws RCSException {
		if (eventsMap.containsKey(id) == false) {
			throw new RCSException("Event " + id + " not found");
		}
		
		Event e = eventsMap.get(id);
		
		if (e == null) {
			throw new RCSException("Event " + id + " is null");
		}
		
		return e;
	}
	
	public Option getOption(int id) throws RCSException {
		if (optionsMap.containsKey(id) == false) {
			throw new RCSException("Option " + id + " not found");			
		}
		
		Option o = optionsMap.get(id);
		
		if (o == null) {
			throw new RCSException("Option " + id + " is null");
		}
		
		return o;
	}
}
