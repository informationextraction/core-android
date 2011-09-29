package com.android.service.module;

import com.android.service.StateRun;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

public abstract class BaseInstantAgent extends BaseModule {
	private static final String TAG = "BaseInstantAgent";

	public synchronized void run() {
		status = StateRun.STARTING;

		try {
			actualStart();
			status = StateRun.STARTED;
		} catch (final Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
				Check.log(TAG + " Error: " + ex); //$NON-NLS-1$
			}
		}
		status = StateRun.STOPPED;
	}

	@Override
	public final void actualGo() {

	}

	@Override
	public final void actualStop() {

	}

}
