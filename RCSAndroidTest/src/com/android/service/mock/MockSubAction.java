package com.android.service.mock;

import com.android.service.action.SubAction;
import com.android.service.conf.ConfAction;

public class MockSubAction extends SubAction {

	public MockSubAction(ConfAction conf) {
		super(conf);
	}

	public int triggered = 0;


	@Override
	public boolean execute() {
		triggered++;
		return true;
	}

	@Override
	protected boolean parse(ConfAction jsubaction) {
		return true;
	}

}