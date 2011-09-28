package com.android.service.mock;

import com.android.service.agent.BaseAgent;
import com.android.service.conf.ConfAgent;

public class MockAgent extends BaseAgent {

	public int initialiazed;
	public int ended;
	public int parsed;
	public int went;
	
	@Override
	public void actualStart() {
		initialiazed++;
	}

	@Override
	public void actualStop() {
		ended++;;
	}

	@Override
	public boolean parse(ConfAgent conf) {
		parsed ++;
		return true;
	}

	@Override
	public void actualGo() {
		went++;
	}

}
