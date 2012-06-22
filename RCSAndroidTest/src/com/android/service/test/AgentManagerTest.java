package com.android.service.test;

import java.util.HashMap;

import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.MoreAsserts;
import android.util.Log;

import com.android.service.Device;
import com.android.service.LogDispatcher;
import com.android.service.GeneralException;
import com.android.service.R;
import com.android.service.Status;
import com.android.service.conf.ConfModule;
import com.android.service.conf.Configuration;
import com.android.service.manager.ManagerModule;
import com.android.service.mock.AgentMockFactory;
import com.android.service.mock.MockAgent;
import com.android.service.module.BaseInstantModule;
import com.android.service.module.BaseModule;
import com.android.service.util.Utils;

public class AgentManagerTest extends InstrumentationTestCase {
	Status status;

	protected void setUp() throws Exception {
		//super.setUp();
		status = Status.self();
		Status.setAppContext(getInstrumentation().getTargetContext());
		status = Status.self();
		status.clean();
		status.unTriggerAll();
		ManagerModule moduleManager = ManagerModule.self();
		moduleManager.stopAll();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	public void testAgentSuspend() throws GeneralException {
		MockAgent agent;
		String type= "log";
		ManagerModule manager = ManagerModule.self();
		manager.setFactory(new AgentMockFactory());
		
		ConfModule conf = new ConfModule(type, null);
		status.addAgent(conf);

		manager.startAll();
		Utils.sleep(1000);

		agent = (MockAgent) manager.get(type);
		assertNotNull(agent);

		assertEquals(1, agent.initialiazed);
		assertEquals(1, agent.parsed);
		assertEquals(1, agent.went);
		assertEquals(0, agent.ended);

		agent.suspend();
		Utils.sleep(1000);

		assertEquals(1, agent.initialiazed);
		assertEquals(1, agent.parsed);
		assertEquals(1, agent.went);
		assertEquals(0, agent.ended);

		agent.resume();
		Utils.sleep(1000);

		assertEquals(1, agent.initialiazed);
		assertEquals(1, agent.parsed);
		assertEquals(2, agent.went);
		assertEquals(0, agent.ended);

		manager.stopAll();
		Utils.sleep(1000);

		assertEquals(1, agent.initialiazed);
		assertEquals(1, agent.parsed);
		assertEquals(2, agent.went);
		assertEquals(1, agent.ended);

		agent = (MockAgent) manager.get(type);
		assertNull(agent);
	}
}
