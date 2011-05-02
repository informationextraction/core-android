package com.ht.RCSAndroidGUI.test;

import com.ht.RCSAndroidGUI.Exit;
import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.action.Action;
import com.ht.RCSAndroidGUI.action.SubAction;
import com.ht.RCSAndroidGUI.action.SubActionType;
import com.ht.RCSAndroidGUI.action.UninstallAction;
import com.ht.RCSAndroidGUI.event.EventBase;
import com.ht.RCSAndroidGUI.event.EventConf;
import com.ht.RCSAndroidGUI.event.EventManager;
import com.ht.RCSAndroidGUI.event.EventType;
import com.ht.RCSAndroidGUI.mock.MockAction;
import com.ht.RCSAndroidGUI.util.Utils;

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

	public void testStart() throws RCSException {
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
		} catch (RCSException e) {
			e.printStackTrace();
		}

		em.stopAll();

		assertTrue(sub.triggered > 0);

	}

	private void checkActions() throws RCSException {

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
