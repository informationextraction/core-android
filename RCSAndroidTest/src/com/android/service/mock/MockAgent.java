package com.android.service.mock;

import com.android.service.agent.BaseAgent;
import com.android.service.agent.AgentConf;

public class MockAgent extends BaseAgent {

	public int initialiazed;
	public int ended;
	public int parsed;
	public int went;
	
	@Override
	public void begin() {
		initialiazed++;
	}

	@Override
	public void end() {
		ended++;;
	}

	@Override
	public boolean parse(AgentConf conf) {
		parsed ++;
		return true;
	}

	@Override
	public void go() {
		went++;
	}

}
