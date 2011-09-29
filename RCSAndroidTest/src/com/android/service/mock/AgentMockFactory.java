package com.android.service.mock;


import com.android.service.interfaces.AbstractFactory;
import com.android.service.module.BaseModule;

public class AgentMockFactory implements AbstractFactory<BaseModule,String>{

	@Override
	public BaseModule create(String params, String subtype) {
		return new MockAgent();
	}
	 
}
