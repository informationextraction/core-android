package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.action.SubAction;

public class MockAction extends SubAction {

	public int triggered = 0;

	public MockAction(int id) {
		super(id, null);
	}

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