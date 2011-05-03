package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentConf;

public class MockAgent extends AgentBase {

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
