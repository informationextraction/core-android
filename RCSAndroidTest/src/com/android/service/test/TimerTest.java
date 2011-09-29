package com.android.service.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.action.Action;
import com.android.service.action.SubAction;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.conf.ConfEvent;
import com.android.service.manager.ManagerEvent;
import com.android.service.mock.MockSubAction;
import com.android.service.util.Check;
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
		
		MockSubAction sub = addActionCounter();
		ManagerEvent em = ManagerEvent.self();

		int max = 10;
		int action = 0;

		// dalla partenza aspetta 0 giorni, 
		String jsonConf = "{\"event\":\"afterinst\",\"desc\":\"afterinst test\",\"enabled\":true,\"days\":0,\"start\":0,\"delay\":300}";

		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < max; i++) {
			final ConfEvent e = new ConfEvent(i, conf);
			e.startAction=i;
			status.addEvent(e);
		}

		em.startAll();
		Utils.sleep(1000);
		em.stopAll();
		
		int[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
		//int[] triggeredSlow = status.getTriggeredActions(Action.SLOW_QUEUE);
		status.unTriggerAll();
		assertTrue(triggeredFast.length == max);
		
		
	}

	public void testLoopTimer() throws JSONException {

		int iter = 5;
		addTimerLoopEvent(1, 100, iter);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();
		Utils.sleep(1000);
		int[] triggeredFast = status.getTriggeredActions(Action.FAST_QUEUE);
		//int[] triggeredSlow = status.getTriggeredActions(Action.SLOW_QUEUE);
		status.unTriggerAll();
		assertTrue(triggeredFast.length == 1);
		//assertTrue(triggeredSlow.length == 0);

		Utils.sleep(1000);
		triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
		//triggeredSlow = status.getTriggeredActions(Action.SLOW_QUEUE);
		status.unTriggerAll();
		assertTrue(triggeredFast.length == 1);
		//assertTrue(triggeredSlow.length == 0);

		em.stopAll();
	}

	public void testTimerLoopMockAction() throws JSONException {

		MockSubAction sub = addActionCounter();

		addTimerLoopEvent(1, 100, 5);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();

		try {
			for (int i = 0; i < 5; i++) {
				checkActionsFast();
				Utils.sleep(100);
			}
		} catch (GeneralException e) {
			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}
		
		Utils.sleep(1000);
		
		int[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
		assertTrue(triggeredFast.length == 0);

		em.stopAll();

		assertTrue(sub.triggered > 0);

	}

	private MockSubAction addActionCounter() throws JSONException {
		Action action = new Action(0, "action0");
		String jsonConf = "{\"action\"=>\"counter\"}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();

		MockSubAction sub = new MockSubAction(new ConfAction(0, 0, conf));
		action.addSubAction(sub);
		status.addAction(action);
		return sub;
	}

	private void checkActionsFast() throws GeneralException {

		int[] actionIds = status.getTriggeredActions(Action.SLOW_QUEUE);
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

	private void addTimerLoopEvent(int num, int delay, int iter) throws JSONException {
		String jsonConf = "{\"event\"=>\"timer\",\"_mig\"=>true,\"desc\"=>\"timer\",\"enabled\"=>true,\"ts\"=>\"00:00:00\",\"te\"=>\"23:59:59\",\"repeat\"=>0,\"delay\"=>100}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < num; i++) {
			int id = i;
			int action = i;
			ConfEvent e = new ConfEvent(i, "timer", conf);
			e.delay = delay;
			e.iter = iter;
			Status.self().addEvent(e);
		}
	}
}
