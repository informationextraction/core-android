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
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerProcess;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

public class EventProcess extends BaseEvent implements Observer<ProcessInfo> {
	/** The Constant TAG. */
	private static final String TAG = "EventProcess"; //$NON-NLS-1$

	private int actionOnEnter, actionOnExit;
	private boolean active = false;
	private String starname;
	private boolean window;
	private boolean focus;

	@Override
	public void actualStart() {
		ListenerProcess.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerProcess.self().detach(this);
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			window = conf.getBoolean("window");
			focus = conf.getBoolean("focus");
			starname = conf.getString("process");
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


}
