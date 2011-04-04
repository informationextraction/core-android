/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.action;

import com.ht.RCSAndroidGUI.RCSException;

public class Action {
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

		SubAction sub = SubAction.factory(type, params);

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

