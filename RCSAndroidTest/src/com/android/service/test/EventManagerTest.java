package com.android.service.test;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.android.service.Exit;
import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.Trigger;
import com.android.service.action.Action;
import com.android.service.action.SubAction;

import com.android.service.action.UninstallAction;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.Configuration;
import com.android.service.event.BaseEvent;

import com.android.service.manager.ManagerEvent;
import com.android.service.mock.MockActionCounter;
import com.android.service.mock.MockSubAction;
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
		ManagerEvent em = ManagerEvent.self();

		int max = 10;
		int action = 0;

		String jsonConf = "{\"event\":\"timer\",\"_mig\":true,\"desc\":\"position loop\",\"enabled\":true,\"ts\":\"00:00:00\",\"te\":\"23:59:59\",\"repeat\":8,\"delay\":300}";

		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < max; i++) {
			final ConfEvent e = new ConfEvent(i, "timer", conf);
			status.addEvent(e);
		}

		em.startAll();
		Utils.sleep(10);
		em.stopAll();
	}

	public void testSingleTrigger() throws JSONException, GeneralException {

		int num = 1;
		addTimerLoopEvent(num,100,1);
		new MockActionCounter(0);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();		
		checkActionsFast(true);
		
		Utils.sleep(1000);

		Utils.sleep(1000);
		Trigger[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
		//triggeredSlow = status.getTriggeredActions(Action.SLOW_QUEUE);
		status.unTriggerAll();
		assertTrue(triggeredFast.length == num);
		//assertTrue(triggeredSlow.length == 0);

		em.stopAll();
	}
	
	

	public void testManyTriggers() throws JSONException, GeneralException {
		//assertTrue(false);
		
		int num = 10;
		addTimerLoopEvent(num, 100, Integer.MAX_VALUE);
		MockActionCounter action = new MockActionCounter(0);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();
		Utils.sleep(1000);
		
		for(int i=0;i<num*4;i++){
			checkActionsFast(false);
			Utils.sleep(100);
		}		
				
		Trigger[] triggeredFast = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);								
		assertTrue(triggeredFast.length >= 1);
		status.unTriggerAll();
		
		assertTrue(action.getTriggered() >= num);

		em.stopAll();
	}

	private void addTimerLoopEvent(int i) throws JSONException {
		int delay=100;
		int iter=10;
		addTimerLoopEvent(i,delay,iter);
	}

	private void addTimerLoopEvent(int num, int delay, int iter) throws JSONException {
		String jsonConf = "{\"event\"=>\"timer\",\"_mig\"=>true,\"desc\"=>\"timer\",\"enabled\"=>true,\"ts\"=>\"00:00:00\",\"te\"=>\"23:59:59\",\"repeat\"=>0,\"delay\"=>100}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
		for (int i = 0; i < num; i++) {
			int id = i;
			int action = i;
			ConfEvent e = new ConfEvent(i, "timer", conf);
			e.delay=delay;
			e.iter=iter;
			Status.self().addEvent(e);
		}
	}

	public void testSingleMockAction() throws JSONException {

		/*Action action = new Action(0, "action0");
		String jsonConf = "{\"action\"=>\"counter\"}";
		JSONObject conf = (JSONObject) new JSONTokener(jsonConf).nextValue();
				
		MockSubAction sub = new MockSubAction(new ConfAction(0,0,conf ));
		action.addSubAction(sub);
		status.addAction(action);*/
		
		MockActionCounter action = new MockActionCounter(0);

		int iter = 5;
		addTimerLoopEvent(1,500,iter);

		ManagerEvent em = ManagerEvent.self();

		em.startAll();

		try {
			for (int i = 0; i < iter; i++) {
				checkActionsFast(true);
				//Utils.sleep(2000);
			}
		} catch (GeneralException e) {
			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}

		em.stopAll();

		assertTrue(action.getTriggered() == iter);

	}

	private void checkActionsFast(boolean blocking) throws GeneralException {

		Trigger[] actionIds;
		
		if(blocking){
			actionIds = status.getTriggeredActions(Action.FAST_QUEUE);
		}else{
			actionIds = status.getNonBlockingTriggeredActions(Action.FAST_QUEUE);
		}
		for (int i = 0; i < actionIds.length; i++) {
			Trigger trigger = actionIds[i];
			final Action action = status.getAction(trigger.getActionId());
			executeAction(action);
		}
		
	}

	private void executeAction(Action action) {
		for (SubAction sub : action.getSubActions()) {
			sub.execute(new Trigger(0,null));
		}
	}

}
