package com.ht.RCSAndroidGUI.mock;

import com.ht.RCSAndroidGUI.event.EventBase;
import com.ht.RCSAndroidGUI.event.EventConf;

public class MockEvent extends EventBase {

	public int initialiazed=0;
	public int ended=0;
	public int parsed;
	public int went;
	
	@Override
	public void begin() {
		initialiazed++;
	}

	@Override
	public void end() {
		ended++;;
	}

	@Override
	public void go() {
		went++;
	}

	@Override
	public boolean parse(EventConf event) {
		parsed ++;
		return true;
	}

}
