package com.android.service.mock;

import com.android.service.agent.AgentBase;
import com.android.service.agent.AgentType;
import com.android.service.interfaces.AbstractFactory;

public class AgentMockFactory implements AbstractFactory<AgentBase,AgentType>{

	@Override
	public AgentBase create(AgentType params) {
		return new MockAgent();
	}
	 
}
