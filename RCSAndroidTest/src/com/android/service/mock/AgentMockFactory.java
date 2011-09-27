package com.android.service.mock;

import com.android.service.agent.BaseAgent;

import com.android.service.interfaces.AbstractFactory;

public class AgentMockFactory implements AbstractFactory<BaseAgent,String>{

	@Override
	public BaseAgent create(String params) {
		return new MockAgent();
	}
	 
}
