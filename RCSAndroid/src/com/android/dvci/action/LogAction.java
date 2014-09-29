/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action;

import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.util.Check;


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
	public LogAction(final ConfAction params) {
		super(params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.action.SubAction#execute()
	 */
	@Override
	public boolean execute(Trigger trigger) {
		if(Cfg.DEBUG){
			Check.log(TAG + " (LogAction) logging: "+ msg);
		}
		EvidenceBuilder.info(msg);

		return true;
	}

	@Override
	protected boolean parse(ConfAction params) {

		try {
			this.msg = params.getString("text");
		} catch (ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}

		return true;
	}
}
