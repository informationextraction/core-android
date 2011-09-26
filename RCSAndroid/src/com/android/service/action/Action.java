/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 **********************************************/

package com.android.service.action;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.service.GeneralException;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

/**
 * The Class Action.
 */
public class Action {
	private static final String TAG = "Action"; //$NON-NLS-1$

	/** The Constant ACTION_NULL. */
	public static final int ACTION_NULL = -1;

	/** Action array. */
	private final List<SubAction> list;

	/** Action ID. */
	private final int actionId;

	private final String desc;

	/**
	 * Action constructor.
	 * 
	 * @param id
	 *            : action id
	 * @param desc
	 * @param num
	 *            : number of subactions
	 * @throws GeneralException
	 *             the RCS exception
	 */
	public Action(final int id, String desc) {
		if (Cfg.DEBUG) {
			Check.asserts(id >= 0, "Invalid id"); //$NON-NLS-1$
		}

		this.actionId = id;
		this.desc = desc;
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
	 * @param jsubaction
	 *            the params
	 * @throws GeneralException
	 *             the RCS exception
	 * @throws JSONException 
	 * @throws ConfigurationException 
	 */
	public boolean addSubAction(final ActionConf actionConf) throws GeneralException, ConfigurationException {

		if (actionConf.getType() != null) {
			final SubAction sub = SubAction.factory(actionConf.getType(), actionConf);
			if(sub==null){
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (addSubAction): unknown type: " + actionConf.getType());//$NON-NLS-1$
				}
				return false;
			}
			list.add(sub);
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (addSubAction): null type " );//$NON-NLS-1$
			}
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
			throw new GeneralException("Subaction index above SubAction array boundary"); //$NON-NLS-1$
		}

		return list.get(index);
	}

	public SubAction[] getSubActions() {
		return list.toArray(new SubAction[] {});
	}
}
