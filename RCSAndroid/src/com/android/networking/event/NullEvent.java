package com.android.networking.event;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfEvent;
import com.android.networking.util.Check;

public class NullEvent extends BaseEvent {
	private static final String TAG = "NullEvent";

	@Override
	protected boolean parse(ConfEvent event) {
		return true;
	}

	@Override
	protected void actualGo() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualGo): null event"); //$NON-NLS-1$
		}
	}

	@Override
	protected void actualStart() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStart): null event"); //$NON-NLS-1$
		}
	}

	@Override
	protected void actualStop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStop): null event"); //$NON-NLS-1$
		}
	}

}
