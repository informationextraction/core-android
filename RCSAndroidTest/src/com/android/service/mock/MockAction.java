package com.android.service.mock;

import com.android.service.action.SubAction;
import com.android.service.conf.ConfAction;

public class MockAction extends SubAction {

	public MockAction(ConfAction conf) {
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
		// TODO Auto-generated method stub
		return false;
	}

}