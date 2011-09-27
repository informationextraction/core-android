package com.android.service.mock;

import com.android.service.event.BaseEvent;
import com.android.service.event.EventConf;

public class MockEvent extends BaseEvent {

	public int initialiazed=0;
	public int ended=0;
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
	public void go() {
		went++;
	}

	@Override
	public boolean parse(EventConf event) {
		parsed ++;
		return true;
	}

}
