/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.agent;

import com.android.service.ThreadBase;
import com.android.service.conf.ConfigurationException;

/**
 * The Class AgentBase.
 */
public abstract class AgentBase extends ThreadBase {
	private static final String TAG = "AgentBase"; //$NON-NLS-1$
	private String type;

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 * @throws ConfigurationException 
	 */
	public abstract boolean parse(AgentConf conf) throws ConfigurationException;
	
	public int numMarkups(){
		return 1;
	}


	public void setType(String key) {
		this.type=key;
	}
	
	public String getType(){
		return type;
	}

}