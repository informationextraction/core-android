package com.android.deviceinfo.module;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.event.BaseEvent;
import com.android.deviceinfo.interfaces.Observer;

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
