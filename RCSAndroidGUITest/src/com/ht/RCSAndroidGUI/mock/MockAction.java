package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.action.SubAction;
import com.ht.RCSAndroidGUI.action.SubActionType;

public class MockAction extends SubAction {

	public MockAction(SubActionType type) {
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