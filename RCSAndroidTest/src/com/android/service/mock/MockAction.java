package com.android.service.mock;

import com.android.service.action.ActionConf;
import com.android.service.action.SubAction;

public class MockAction extends SubAction {

	public MockAction(ActionConf conf) {
		super(conf);
	}

	public int triggered = 0;


	@Override
	public boolean execute() {
		triggered++;
		return true;
	}

	@Override
	protected boolean parse(ActionConf jsubaction) {
		// TODO Auto-generated method stub
		return false;
	}

}