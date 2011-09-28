package com.android.service.mock;

import com.android.service.conf.ConfEvent;
import com.android.service.event.BaseEvent;

public class MockEvent extends BaseEvent {

	public int initialiazed=0;
	public int ended=0;
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
	public void actualGo() {
		went++;
	}

	@Override
	public boolean parse(ConfEvent event) {
		parsed ++;
		return true;
	}

}
