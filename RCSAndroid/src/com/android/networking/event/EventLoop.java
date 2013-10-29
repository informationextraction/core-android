package com.android.networking.event;

import com.android.networking.conf.ConfEvent;

public class EventLoop extends BaseTimer {

	@Override
	protected boolean parse(ConfEvent event) {

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
		onExit();
	}

}
