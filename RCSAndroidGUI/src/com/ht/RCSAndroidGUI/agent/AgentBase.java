/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.agent;

import android.util.Log;

import com.ht.RCSAndroidGUI.StateRun;
import com.ht.RCSAndroidGUI.ThreadBase;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentBase.
 */
public abstract class AgentBase extends ThreadBase implements Runnable {
	private static final String TAG = "AgentBase";

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public abstract boolean parse(AgentConf conf);

}