package com.android.service.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.conf.ConfEvent;
import com.android.service.event.EventManager;
import com.android.service.util.Utils;

import android.test.AndroidTestCase;

public class TimerTest extends AndroidTestCase {
	Status status;
	
	@Override
	public void setUp() {
		Status.setAppContext(getContext());
		status = Status.self();
		status.clean();
		status.unTriggerAll();
	}

	public void testEventAfterinst() throws GeneralException, JSONException {
		EventManager em = EventManager.self();

		int max = 10;
		int action = 0;

		// dalla partenza aspetta 1 giorno, poi ripete ogni 300 secondi il
		String jsonConf = "{\"event\":\"afterinst\",\"desc\":\"afterinst test\",\"enabled\":true,\"days\":1,\"repeat\":0,\"delay\":300}";

		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < max; i++) {
			final ConfEvent e = new ConfEvent(i, conf);
			status.addEvent(e);
		}

		em.startAll();
		Utils.sleep(10);
		em.stopAll();
	}
}
