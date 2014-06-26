/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : StopAgentAction.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.action;

import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.manager.ManagerModule;
import com.android.dvci.util.Check;

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
