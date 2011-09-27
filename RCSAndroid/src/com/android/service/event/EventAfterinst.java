package com.android.service.event;

import com.android.service.conf.ConfigurationException;

public class EventAfterinst extends BaseTimer {

	private int days;

	@Override
	protected boolean parse(EventConf conf) {		
		try {
			days=conf.getInt("days");
		} catch (ConfigurationException e) {
			return false;
		}
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub

	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

}
