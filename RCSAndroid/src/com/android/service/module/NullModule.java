package com.android.service.module;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfModule;
import com.android.service.util.Check;

public class NullModule extends BaseModule {
	private static final String TAG = "NullModule";
	
	@Override
	protected boolean parse(ConfModule conf) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (parse): null module"); //$NON-NLS-1$
		}
		
		return true;
	}

	@Override
	protected void actualGo() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualGo): null module"); //$NON-NLS-1$
		}
	}

	@Override
	protected void actualStart() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStart): null module"); //$NON-NLS-1$
		}
	}

	@Override
	protected void actualStop() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (actualStop): null module"); //$NON-NLS-1$
		}
	}
}
