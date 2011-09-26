/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.evidence.Evidence;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

// TODO: Auto-generated Javadoc
/**
 * The Class LogAction.
 */
public class LogAction extends SubAction {
	private static final String TAG = "LogAction"; //$NON-NLS-1$
	private String msg;

	/**
	 * Instantiates a new log action.
	 * 
	 * @param params
	 *            the conf params
	 */
	public LogAction(final ActionConf params) {
		super(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute() {
		Evidence.info(msg);

		return true;
	}

	@Override
	protected boolean parse(ActionConf params) {

		try {
			this.msg = params.getString("text");
		} catch (ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		return true;
	}
}
