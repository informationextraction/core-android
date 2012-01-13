/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SubActionType.java
 * Created      : 6-mag-2011
 * Author		: zeno
 * *******************************************/

package com.android.service.action;

public class SubActionType {

	/** The Constant ACTION_SYNC. */
	public final static int ACTION_SYNC = 0x4001; // Sync su server

	/** The Constant ACTION_UNINSTALL. */
	public final static int ACTION_UNINSTALL = 0x4002; // Uninstall

	/** The Constant ACTION_RELOAD. */
	public final static int ACTION_RELOAD = 0x4003; // Reload della backdoor

	/** The Constant ACTION_SMS. */
	public final static int ACTION_SMS = 0x4004; // Invia un SMS

	/** The Constant ACTION_TOOTHING. */
	// public final int ACTION_TOOTHING=0x4005; // Non utilizzata

	/** The Constant ACTION_START_AGENT. */
	public final static int ACTION_START_AGENT = 0x4006; // Avvia un agente

	/** The Constant ACTION_STOP_AGENT. */
	public final static int ACTION_STOP_AGENT = 0x4007; // Ferma un agente

	/** The Constant ACTION_SYNC_PDA. */
	public final static int ACTION_SYNC_PDA = 0x4008; // Sync su Mediation Node

	/** The Constant ACTION_EXECUTE. */
	public final static int ACTION_EXECUTE = 0x4009; // Esegui un comando

	/** The Constant ACTION_SYNC_APN. */
	public final static int ACTION_SYNC_APN = 0x400a; // Sync su APN

	/** The Constant ACTION_LOG. */
	public final static int ACTION_LOG = 0x400b; // Crea un LOG_INFO

}
