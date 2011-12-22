package com.android.service.test;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.Trigger;
import com.android.service.action.Action;
import com.android.service.action.SubAction;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.conf.ConfEvent;
import com.android.service.event.BaseEvent;
import com.android.service.manager.ManagerEvent;
import com.android.service.mock.MockActionCounter;
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
				
		ManagerEvent em = ManagerEvent.self();

		int max = 10;
		MockActionCounter[] array = new MockActionCounter[max];
		for (int i = 0; i < max; i++) {
			array[i]=new MockActionCounter(i);
		}
		
		// dalla partenza aspetta 0 giorni, 
		String jsonConf = "{\"event\":\"afterinst\",\"desc\":\"afterinst test\",\"enabled\":true,\"days\":0,\"start\":0,\"delay\":300}";

		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < max; i++) {
			final ConfEvent e = new ConfEvent(i, conf);
			e.startAction=i;
			boolean ret=status.addEvent(e);
		}

		HashMap<Integer, BaseEvent> instances = em.getInstances();
		assertTrue(instances.size()==0);
		
		em.startAll();
				
		Utils.sleep(1000);
		instances = em.getInstances();
		assertTrue(instances.size()==max);
		em.stopAll();
		
		Trigger[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);		
		assertTrue(triggeredFast.length == max);
		status.unTriggerAll();
		/*for (int i = 0; i < max; i++) {
			MockActionCounter action = array[i];
			assertTrue(action.getTriggered() == max);
		}*/
		
		
	}

	public void notestLoopTimer() throws JSONException {

		//assertTrue(false);
		
		int iter = 5;
		addTimerLoopEvent(1, 100, iter);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();
		Utils.sleep(1000);
		Trigger[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
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

	private void checkActionsFast() throws GeneralException {

		Trigger[] actionIds = status.getTriggeredActions(Action.MAIN_QUEUE);
		for (int i = 0; i < actionIds.length; i++) {
			final Action action = status.getAction(actionIds[i].getActionId());
			executeAction(action);
		}

	}

	private void executeAction(Action action) {
		for (SubAction sub : action.getSubActions()) {
			sub.execute(new Trigger(0, null));
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
