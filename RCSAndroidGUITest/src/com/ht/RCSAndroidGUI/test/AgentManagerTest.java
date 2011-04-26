package com.ht.RCSAndroidGUI.test;

import java.util.HashMap;
import java.util.Set;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.Device;
import com.ht.RCSAndroidGUI.LogDispatcher;
import com.ht.RCSAndroidGUI.R;
import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.agent.AgentConf;
import com.ht.RCSAndroidGUI.agent.AgentBase;
import com.ht.RCSAndroidGUI.agent.AgentManager;
import com.ht.RCSAndroidGUI.agent.AgentType;
import com.ht.RCSAndroidGUI.conf.Configuration;
import com.ht.RCSAndroidGUI.util.Utils;

import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import android.util.Log;

public class AgentManagerTest extends AndroidTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		Status.setAppContext(getContext());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAgentsStart() throws InterruptedException, RCSException {
		Resources resources = getContext().getResources();

		final byte[] resource = Utils.inputStreamToBuffer(
				resources.openRawResource(R.raw.config), 8); // config.bin

		// Initialize the configuration object
		final Configuration conf = new Configuration(resource);

		// Identify the device uniquely
		final Device device = Device.self();

		// Load the configuration
		conf.LoadConfiguration();

		// Start log dispatcher
		final LogDispatcher logDispatcher = LogDispatcher.self();
		logDispatcher.start();

		// Start agents
		AgentManager agentManager = AgentManager.self();
		
		HashMap<AgentType, AgentBase> agentsMap = agentManager.getRunning();
		AgentBase[] agentsList = agentsMap.values().toArray(new AgentBase[]{});
		MoreAsserts.assertEmpty(agentsMap);

		agentManager.startAll();
		Utils.sleep(2000);
		
		agentsMap = agentManager.getRunning();
		MoreAsserts.assertNotEmpty(agentsMap);
		agentsList = agentsMap.values().toArray(new AgentBase[]{});
		for (AgentBase agent : agentsList) {
			assertTrue(agent.getStatus() == AgentConf.AGENT_RUNNING);
		}
		assertEquals(agentsList.length,2);
		/*
		 * agentManager.stopAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 * agentManager.startAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 * agentManager.restartAgent(Agent.AGENT_DEVICE); Utils.sleep(2000);
		 */
		// Stop agents
		agentManager.stopAll();
		Utils.sleep(2000);
		
		for (AgentBase agent : agentsList) {
			assertTrue(agent.getStatus() == AgentConf.AGENT_STOPPED);
		}
		

		// Ci stiamo chiudendo
		logDispatcher.halt();
		logDispatcher.join();

		Log.d("RCS", "LogDispatcher Killed");
	}
}
