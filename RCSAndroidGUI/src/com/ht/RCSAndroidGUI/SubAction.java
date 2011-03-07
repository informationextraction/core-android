/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 03-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI;

public class SubAction {
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
	
	int getSubActionType() {
		return subActionType;
	}
	
	byte[] getSubActionParams() {
		return subActionParams;
	}
}
