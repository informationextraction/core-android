package com.android.service.mock;

import com.android.service.action.SubAction;
import com.android.service.action.SubActionType;

public class MockAction extends SubAction {

	public MockAction(int type) {
		super(type, null);
	}

	public int triggered = 0;

	@Override
	protected boolean parse(byte[] params) {
		return true;
	}

	@Override
	public boolean execute() {
		triggered++;
		return true;
	}

}