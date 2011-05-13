package com.android.service.test;

import com.android.service.Exit;
import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.action.Action;
import com.android.service.action.SubAction;
import com.android.service.action.SubActionType;
import com.android.service.action.UninstallAction;
import com.android.service.conf.Configuration;
import com.android.service.event.EventBase;
import com.android.service.event.EventConf;
import com.android.service.event.EventManager;
import com.android.service.event.EventType;
import com.android.service.mock.MockAction;
import com.android.service.util.Utils;

import android.test.AndroidTestCase;
import android.util.Log;

public class EventManagerTest extends AndroidTestCase {
	Status status;

	@Override
	public void setUp() {
		status = Status.self();
		status.clean();
		status.unTriggerAll();
	}

	public void testStart() throws GeneralException {
		EventManager em = EventManager.self();

		int max = 10;

		int action = 0;
		// every second (type, lo, hi)
		byte[] params = new byte[] { 0x01, 0x00, 0x00, 0x00, (byte) 0xE8, 0x03,
				0x00, 0x00, 0x01, 0x00, 0x00, 0x00 };

		for (int i = 0; i < max; i++) {
			final EventConf e = new EventConf(EventType.EVENT_TIMER, i, 0,
					params);
			status.addEvent(e);
		}

		em.startAll();
		Utils.sleep(10);
		em.stopAll();
	}

	public void testTrigger() {

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

	public void testManyTriggers() {

		int num = 100;
		addTimerEvent(100);

		EventManager em = EventManager.self();

		em.startAll();
		Utils.sleep(5000);

		int[] triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == num);

		Utils.sleep(5000);

		triggered = status.getTriggeredActions();
		status.unTriggerAll();
		assertTrue(triggered.length == num);

		em.stopAll();
	}

	private void addTimerEvent(int num) {
		byte[] params = new byte[] { 0x01, 0x00, 0x00, 0x00, (byte) 0xE8, 0x03,
				0x00, 0x00, 0x01, 0x00, 0x00, 0x00 };

		for (int i = 0; i < num; i++) {
			int id = i;
			int action = i;
			EventConf e = new EventConf(EventType.EVENT_TIMER, id, action,
					params);
			Status.self().addEvent(e);
		}
	}

	public void testSingleMockAction() {
		Action action = new Action(0);
		MockAction sub = new MockAction(SubActionType.ACTION_LOG);
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
			if(Configuration.DEBUG) { e.printStackTrace(); }
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
