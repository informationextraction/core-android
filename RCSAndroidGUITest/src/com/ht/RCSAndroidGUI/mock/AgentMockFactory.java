package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;

public class AgentMockFactory implements AbstractFactory<AgentBase,AgentType>{

	@Override
	public AgentBase create(AgentType params) {
		return new MockAgent();
	}
	 
}
