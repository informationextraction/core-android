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

import com.android.service.GeneralException;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfAction;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

/**
 * The Class Action.
 */
public class Action {
	private static final String TAG = "Action"; //$NON-NLS-1$

	/** The Constant ACTION_NULL. */
	public static final int ACTION_NULL = -1;

	/** Coda per tutte le action che non interagiscono con il core */
	public static final int FAST_QUEUE = 0;

	/** Coda per la sync, execute e uninstall */
	public static final int MAIN_QUEUE = 1;

	public static final int NUM_QUEUE = 2;
	/** Action array. */
	private final List<SubAction> list;

	/** Action ID. */
	private final int actionId;

	private final String desc;

	private int queue = FAST_QUEUE;

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
	public boolean addSubAction(final ConfAction actionConf) throws GeneralException, ConfigurationException {
		if (actionConf.getType() != null) {
			final SubAction sub = SubAction.factory(actionConf.getType(), actionConf);
			
			if (sub == null) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Error (addSubAction): unknown type: " + actionConf.getType());//$NON-NLS-1$
				}
				
				return false;
			}
			
			list.add(sub);
			
			if (sub instanceof SubActionSlow) {
				setQueue(MAIN_QUEUE);
			}
			
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error (addSubAction): null type ");//$NON-NLS-1$
			}
			
			return false;
		}
	}

	private void setQueue(int queue) {
		this.queue = queue;
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
			throw new GeneralException("Out of boundary"); //$NON-NLS-1$
		}

		return list.get(index);
	}

	public SubAction[] getSubActions() {
		return list.toArray(new SubAction[] {});
	}

	public int getQueue() {
		return queue;
	}

	public String getDesc() {
		return desc;
	}

	public String toString() {
		if (Cfg.DEBUG) {
			return getId() + " [" + getDesc().toUpperCase() + "] qq: " + getQueue();
		} else {
			return Integer.toString(getId());
		}
	}
}
