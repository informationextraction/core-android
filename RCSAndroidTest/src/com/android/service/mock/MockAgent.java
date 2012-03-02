package com.android.service.mock;

import com.android.service.conf.ConfModule;
import com.android.service.module.BaseModule;

public class MockAgent extends BaseModule {

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
	public boolean parse(ConfModule conf) {
		parsed ++;
		return true;
	}

	@Override
	public void actualGo() {
		went++;
	}

}
