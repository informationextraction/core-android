package com.android.service.mock;

import com.android.service.agent.AgentBase;

import com.android.service.interfaces.AbstractFactory;

public class AgentMockFactory implements AbstractFactory<AgentBase,String>{

	@Override
	public AgentBase create(String params) {
		return new MockAgent();
	}
	 
}
