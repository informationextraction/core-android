/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.util.Hashtable;
import java.util.Map;

public enum AgentType {

	AGENT_INFO(Types.BASE + 0x0),
	AGENT_SMS(Types.BASE + 0x1), // Agente di cattura delle Email/Sms/Mms
	AGENT_TASK(Types.BASE + 0x2), // Agente per la cattura degli appuntamenti
	AGENT_CALLLIST(Types.BASE + 0x3), // Agente per la cattura della lista delle
										// chiamate
	AGENT_DEVICE(Types.BASE + 0x4), // Agente per la cattura delle informazioni
									// sul device
	AGENT_POSITION(Types.BASE + 0x5), // Agente per la cattura della posizione
										// GPS o celle GSM
	AGENT_CALL(Types.BASE + 0x6), // Agente per la cattura delle chiamate con
									// conference call
	AGENT_CALL_LOCAL(Types.BASE + 0x7), // Agente per la registrazione in situ
										// delle chiamate

	AGENT_KEYLOG(Types.BASE + 0x8), // Agente per la cattura dei tasti battuti
									// su tastiera
	AGENT_SNAPSHOT(Types.BASE + 0x9), // Agente per la cattura degli snapshot
										// delloschermo
	AGENT_URL(Types.BASE + 0xa), // Agente per la cattura degli URL visitati

	AGENT_IM(Types.BASE + 0xb), // Agente per la cattura degli IM
	AGENT_EMAIL(Types.BASE + 0xc), // Non utilizzato (fa tutto AGENT_SMS)
	AGENT_MIC(Types.BASE + 0xd), // Agente per la cattura del microfono
	AGENT_CAM(Types.BASE + 0xe), // Agente per la cattura degli snapshot dalle webcam
	AGENT_CLIPBOARD(Types.BASE + 0xf), // Agente per la cattura degli appunti
	AGENT_CRISIS(Types.BASE + 0x10), // Agente di Crisis
	AGENT_APPLICATION(Types.BASE + 0x11), // Agente per la cattura delle applicazioni avviate o
	// fermate
	AGENT_LIVEMIC(Types.BASE + 0x12),
	AGENT_PDA(0xDF7A); // Solo per PC (infection agent)

	private int value;

	private AgentType(int value) {
		this.value = value;
		Types.map.put(value, this);
	}

	public static AgentType get(int value) {
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

		public static final int BASE = 0x1000;
		/**
		 * map from name no enum constant
		 */
		static final Map<Integer, AgentType> map = new Hashtable<Integer, AgentType>();
	}

}
