/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.interfaces.AbstractFactory;

public class EventFactory implements AbstractFactory<EventBase,EventType>{
	private static final String TAG = "EventFactory";

	public EventBase create(EventType type) {
		EventBase e = null;
		switch (type) {
			case EVENT_TIMER:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "");
				e = new EventTimer();
				break;

			case EVENT_SMS:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_SMS");
				e = new EventSms();
				break;

			case EVENT_CALL:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_CALL");
				e = new EventCall();
				break;

			case EVENT_CONNECTION:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_CONNECTION");
				e = new EventConnectivity();
				break;

			case EVENT_PROCESS:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_PROCESS");
				e = new EventProcess();
				break;

			case EVENT_CELLID:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_CELLID");
				e = new EventCellId();
				break;

			case EVENT_QUOTA:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_QUOTA");
				break;

			case EVENT_SIM_CHANGE:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_SIM_CHANGE");
				e = new EventSim();
				break;

			case EVENT_LOCATION:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_LOCATION");
				e = new EventLocation();
				break;

			case EVENT_AC:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_AC");
				e = new EventAc();
				break;

			case EVENT_BATTERY:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_BATTERY");
				e = new EventBattery();
				break;

			case EVENT_STANDBY:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Info: " + "EVENT_STANDBY");
				e = new EventStandby();
				break;

			default:
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: " + "Unknown: " + type);
				break;
		}
		return e;
	}
}
