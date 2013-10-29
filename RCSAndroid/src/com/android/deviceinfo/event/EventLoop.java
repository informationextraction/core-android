package com.android.deviceinfo.event;

import com.android.deviceinfo.conf.ConfEvent;

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
