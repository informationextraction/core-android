package com.android.service.event;

import com.android.service.conf.ConfEvent;

public class EventLoop extends BaseTimer {

	@Override
	protected boolean parse(ConfEvent event) {
		
		return false;
	}

	@Override
	protected void actualGo() {
		
	}

	@Override
	protected void actualStart() {
		onEnter();
	}

	@Override
	protected void actualStop() {
		onExit();
	}

}
