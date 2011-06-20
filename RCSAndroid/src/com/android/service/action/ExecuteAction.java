/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ExecuteAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class ExecuteAction.
 */
public class ExecuteAction extends SubAction {
	private static final String TAG = "ExecuteAction";
	
	/**
	 * Instantiates a new execute action.
	 * 
	 * @param actionType
	 *            the type
	 * @param confParams
	 *            the conf params
	 */
	public ExecuteAction(final int actionType, final byte[] confParams) {
		super(actionType, confParams);
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
	protected boolean parse(final byte[] params) {
		final DataBuffer databuffer = new DataBuffer(params, 0, params.length);

		try {
			final int len = databuffer.readInt();
			final byte[] buffer = new byte[len];
			databuffer.read(buffer);

			final String command = WChar.getString(buffer, true);

			Log.d("QZ", TAG + " (parse): " + command);
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");
			}
			return false;
		}

		return false;
	}
}
