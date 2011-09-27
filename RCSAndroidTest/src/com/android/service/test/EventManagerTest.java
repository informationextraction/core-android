package com.android.service.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.Exit;
import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.action.Action;
import com.android.service.action.ActionConf;
import com.android.service.action.SubAction;

import com.android.service.action.UninstallAction;
import com.android.service.auto.Cfg;
import com.android.service.conf.Configuration;
import com.android.service.event.BaseEvent;
import com.android.service.event.EventConf;
import com.android.service.event.EventManager;

import com.android.service.mock.MockAction;
import com.android.service.util.Check;
import com.android.service.util.Utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class EventManagerTest extends AndroidTestCase {
	Status status;

	@Override
	public void setUp() {
		Status.setAppContext(getContext());
		status = Status.self();
		status.clean();
		status.unTriggerAll();
	}

	public void testStart() throws GeneralException, JSONException {
		EventManager em = EventManager.self();

		int max = 10;
		int action = 0;

		String jsonConf = "{\"event\":\"timer\",\"_mig\":true,\"desc\":\"position loop\",\"enabled\":true,\"ts\":\"00:00:00\",\"te\":\"23:59:59\",\"repeat\":8,\"delay\":300}";

		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < max; i++) {
			final EventConf e = new EventConf(i, "timer", conf);
			status.addEvent(e);
		}

		em.startAll();
		Utils.sleep(10);
		em.stopAll();
	}

	public void testTrigger() throws JSONException {
		assertTrue(false);
		addTimerEvent(1);

		EventManager em = EventManager.self();

		em.startAll();
		Utils.sleep(5000);

		int[] triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == 1);

		Utils.sleep(5000);

		triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == 1);

		em.stopAll();
	}

	public void testManyTriggers() throws JSONException {

		assertTrue(false);
		
		int num = 10;
		addTimerEvent(100);

		EventManager em = EventManager.self();

		em.startAll();
		Utils.sleep(1000);

		int[] triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == num);

		Utils.sleep(1000);

		triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == num);

		em.stopAll();
	}

	private void addTimerEvent(int num) throws JSONException {
		String jsonConf = "{\"event\"=>\"timer\",\"_mig\"=>true,\"desc\"=>\"position loop\",\"enabled\"=>true,\"ts\"=>\"00:00:00\",\"te\"=>\"23:59:59\",\"repeat\"=>8,\"delay\"=>300}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < num; i++) {
			int id = i;
			int action = i;
			EventConf e = new EventConf(i, "timer", conf);
			Status.self().addEvent(e);
		}
	}

	public void testSingleMockAction() throws JSONException {
		
		assertTrue(false);
		
		Action action = new Action(0, "action0");
		String jsonConf = "{\"desc\"=>\"Destroy Me !\", \"subactions\"=>[{\"action\"=>\"uninstall\"}]}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		
		MockAction sub = new MockAction(new ActionConf(0,0,"uninstall", conf ));
		action.addSubAction(sub);
		status.addAction(action);

		addTimerEvent(1);

		EventManager em = EventManager.self();

		em.startAll();

		try {
			for (int i = 0; i < 5; i++) {
				checkActions();
				Utils.sleep(2000);
			}
		} catch (GeneralException e) {
			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}

		em.stopAll();

		assertTrue(sub.triggered > 0);

	}

	private void checkActions() throws GeneralException {

		int[] actionIds = status.getTriggeredActions();
		for (int actionId : actionIds) {
			final Action action = status.getAction(actionId);
			executeAction(action);
		}
	}

	private void executeAction(Action action) {
		for (SubAction sub : action.getSubActions()) {
			sub.execute();
		}
	}

}
