/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 06-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.util.Log;

// Debugging class
public class Debug {
	public static void StatusActions() {
		Status statusObj = Status.self();
		
		Log.d("RCS", "DEBUG - Status Actions Begins");
		
		try {
			for (int i = 0; i < statusObj.getActionsNumber(); i++) {
				Action a = statusObj.getAction(i);

				Log.d("RCS", "Action Id: " + a.getId() + " sub num: " + a.getSubActionsNum());

				for (int j = 0; j < a.getSubActionsNum(); j++) {
					SubAction s = a.getSubAction(j);

					Log.d("RCS", "  -> SubAction " + j + " Type: " + s.getSubActionType() + 
							" Params len: " + s.getSubActionParams().length);
				}
			}
		} catch (RCSException rcse) {
			rcse.printStackTrace();
			Log.d("RCS", "RCSException detected in Debug.StatusActions()");
		}
		
		Log.d("RCS", "DEBUG - Status Actions Ends");
	}
	
	public static void StatusAgents() {
		Status statusObj = Status.self();

		Log.d("RCS", "DEBUG - Status Agents Begins");
		
		int agentsNumber = statusObj.getAgentsNumber();
		
		// AGENT_APPLICATION is the actual last agent
		for (int i = 0; i < agentsNumber && i < Agent.AGENT_APPLICATION + 2; i++) {
			try {
				Agent a = statusObj.getAgent(Agent.AGENT + i + 1);
				
				Log.d("RCS", "Agent Id: " + a.getId() + " Params len: " + a.getParams().length);
			} catch (RCSException rcse) {
				// No need to print that this agent doesn't exist
				agentsNumber++;
			}
		}

		Log.d("RCS", "DEBUG - Status Agents Ends");
	}
	
	public static void StatusEvents() {
		Status statusObj = Status.self();

		Log.d("RCS", "DEBUG - Status Events Begins");
		
		for (int i = 0; i < statusObj.getEventsNumber(); i++) {
			try {
				Event e = statusObj.getEvent(i);
				
				Log.d("RCS", "Event Id: " + e.getId() + " Event Type: " + e.getType() + " Params len: " + e.getParams().length);
			} catch (RCSException rcse) {
				// No need to print that this agent doesn't exist
			}
		}

		Log.d("RCS", "DEBUG - Status Events Ends");
	}
	
	public static void StatusOptions() {
		Status statusObj = Status.self();

		Log.d("RCS", "DEBUG - Status Options Begins");
		
		int optionsNumber = statusObj.getOptionssNumber();
		
		// CONFIGURATION_WIFIIP is the actual last option
		for (int i = 0; i < optionsNumber && i < Option.CONFIGURATION_WIFIIP + 2; i++) {
			try {
				Option o = statusObj.getOption(Option.CONFIGURATION + i + 1);
				
				Log.d("RCS", "Option Id: " + o.getId() + " Option Type: " + " Params len: " + o.getParams().length);
			} catch (RCSException rcse) {
				// No need to print that this agent doesn't exist
				optionsNumber++;
			}
		}

		Log.d("RCS", "DEBUG - Status Options Ends");
	}
}
