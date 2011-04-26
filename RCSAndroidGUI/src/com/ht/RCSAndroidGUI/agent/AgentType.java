package com.ht.RCSAndroidGUI.agent;

import java.util.Hashtable;
import java.util.Map;

public enum AgentType {

	/** The Constant AGENT_INFO. */
	AGENT_INFO(0x1000 + 0x0),

	/** The Constant AGENT_SMS. */
	AGENT_SMS(0x1000 + 0x1), // Agente di cattura delle
	// Email/Sms/Mms
	/** The Constant AGENT_TASK. */
	AGENT_TASK(0x1000 + 0x2), // Agente per la cattura
	// degli appuntamenti
	/** The Constant AGENT_CALLLIST. */
	AGENT_CALLLIST(0x1000 + 0x3), // Agente per la
	// cattura della
	// lista delle
	// chiamate
	/** The Constant AGENT_DEVICE. */
	AGENT_DEVICE(0x1000 + 0x4), // Agente per la cattura
	// delle informazioni
	// sul device
	/** The Constant AGENT_POSITION. */
	AGENT_POSITION(0x1000 + 0x5), // Agente per la
	// cattura della
	// posizione GPSo
	// celle GSM
	/** The Constant AGENT_CALL. */
	AGENT_CALL(0x1000 + 0x6), // Agente per la cattura
	// delle chiamate con
	// conference call
	/** The Constant AGENT_CALL_LOCAL. */
	AGENT_CALL_LOCAL(0x1000 + 0x7), // Agente per la
	// registrazione in
	// situ delle
	// chiamate
	/** The Constant AGENT_KEYLOG. */
	AGENT_KEYLOG(0x1000 + 0x8), // Agente per la cattura
	// dei tasti battuti su
	// tastiera
	/** The Constant AGENT_SNAPSHOT. */
	AGENT_SNAPSHOT(0x1000 + 0x9), // Agente per la
	// cattura degli
	// snapshot dello
	// schermo
	/** The Constant AGENT_URL. */
	AGENT_URL(0x1000 + 0xa), // Agente per la cattura
	// degli URL visitati
	/** The Constant AGENT_IM. */
	AGENT_IM(0x1000 + 0xb), // Agente per la cattura
	// degli IM
	/** The Constant AGENT_EMAIL. */
	AGENT_EMAIL(0x1000 + 0xc), // Non utilizzato (fa
	// tutto AGENT_SMS)
	/** The Constant AGENT_MIC. */
	AGENT_MIC(0x1000 + 0xd), // Agente per la cattura
	// del microfono
	/** The Constant AGENT_CAM. */
	AGENT_CAM(0x1000 + 0xe), // Agente per la cattura
	// degli snapshot dalle
	// webcam
	/** The Constant AGENT_CLIPBOARD. */
	AGENT_CLIPBOARD(0x1000 + 0xf), // Agente per la
	// cattura degli
	// appunti
	/** The Constant AGENT_CRISIS. */
	AGENT_CRISIS(0x1000 + 0x10), // Agente di Crisis

	/** The Constant AGENT_APPLICATION. */
	AGENT_APPLICATION(0x1000 + 0x11), // Agente per la
	// cattura delle
	// applicazioni
	// avviate o
	// fermate
	/** The Constant AGENT_PDA. */
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
