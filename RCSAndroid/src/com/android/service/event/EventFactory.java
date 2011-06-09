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

public class EventFactory implements AbstractFactory<EventBase, Integer> {
	private static final String TAG = "EventFactory";

	public EventBase create(Integer eventType) {
		EventBase e = null;
		final int type = eventType.intValue();
		switch (type) {
		case EventType.EVENT_TIMER:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "");
			}
			e = new EventTimer();
			break;

		case EventType.EVENT_SMS:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SMS");
			}
			e = new EventSms();
			break;

		case EventType.EVENT_CALL:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CALL");
			}
			e = new EventCall();
			break;

		case EventType.EVENT_CONNECTION:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CONNECTION");
			}
			e = new EventConnectivity();
			break;

		case EventType.EVENT_PROCESS:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_PROCESS");
			}
			e = new EventProcess();
			break;

		case EventType.EVENT_CELLID:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_CELLID");
			}
			e = new EventCellId();
			break;

		case EventType.EVENT_QUOTA:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_QUOTA");
			}
			break;

		case EventType.EVENT_SIM_CHANGE:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_SIM_CHANGE");
			}
			e = new EventSim();
			break;

		case EventType.EVENT_LOCATION:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_LOCATION");
			}
			e = new EventLocation();
			break;

		case EventType.EVENT_AC:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_AC");
			}
			e = new EventAc();
			break;

		case EventType.EVENT_BATTERY:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_BATTERY");
			}
			e = new EventBattery();
			break;

		case EventType.EVENT_STANDBY:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + "EVENT_STANDBY");
			}
			e = new EventStandby();
			break;

		default:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + "Unknown: " + eventType);
			}
			break;
		}
		return e;
	}
}
