/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.agent;

import com.android.service.ThreadBase;

/**
 * The Class AgentBase.
 */
public abstract class AgentBase extends ThreadBase {
	private static final String TAG = "AgentBase"; //$NON-NLS-1$

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 */
	public abstract boolean parse(AgentConf conf);

}