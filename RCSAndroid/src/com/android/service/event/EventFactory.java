/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;
import com.android.service.util.Check;

public class EventFactory implements AbstractFactory<BaseEvent, String> {
	private static final String TAG = "EventFactory"; //$NON-NLS-1$

	public BaseEvent create(String type) {
		BaseEvent e = null;
		if("timer".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventTimer();
		}else if("date".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventTimer();
		}else if("afterinst".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventTimer();
		}else if("sms".equals(type)){

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SMS") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventSms();
		}else if("call".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CALL") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventCall();
		}else if("connection".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CONNECTION") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventConnectivity();
		}else if("process".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_PROCESS") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventProcess();
		}else if("position cell".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CELLID") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventCellId();
		}else if("quota".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_QUOTA") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventQuota();
		}else if("sim".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SIM_CHANGE") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventSim();
		}else if("position gps".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_LOCATION") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventLocation();
		}else if("ac".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_AC") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventAc();
		}else if("battery".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_BATTERY") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventBattery();
		}else if("standby".equals(type)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_STANDBY") ;//$NON-NLS-1$ //$NON-NLS-2$
			}
			e = new EventStandby();
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "Unknown: " + type) ;//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return e;
	}
}
