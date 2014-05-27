package com.android.deviceinfo.module;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.interfaces.Observer;

public class ProcessObserver implements Observer<ProcessInfo> {

	private BaseModule module;

	public ProcessObserver(BaseModule module) {
		this.module = module;
	}

	@Override
	public int notification(ProcessInfo b) {
		this.module.notifyProcess(b);
		return 0;
	}

}
