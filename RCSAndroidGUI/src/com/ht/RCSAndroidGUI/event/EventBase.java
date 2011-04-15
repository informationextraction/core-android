/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import android.util.Log;

import com.ht.RCSAndroidGUI.Status;
import com.ht.RCSAndroidGUI.ThreadBase;
import com.ht.RCSAndroidGUI.action.Action;

// TODO: Auto-generated Javadoc
/**
 * The Class EventBase.
 */
public abstract class EventBase extends ThreadBase implements Runnable {

	/** The Constant TAG. */
	private static final String TAG = "EventBase";

	// Gli eredi devono implementare i seguenti metodi astratti
	/**
	 * Begin.
	 */
	public abstract void begin();

	/**
	 * End.
	 */
	public abstract void end();

	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	public abstract void parse(EventConf event);

	/** The event. */
	protected EventConf event;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public synchronized void run() {
		status = EventConf.EVENT_RUNNING;

		begin();
		loop();
		end();

		status = EventConf.EVENT_STOPPED;
		Log.d("RCS", "EventBase stopped");
	}

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
		final int actionId = event.getAction();
		if (actionId != Action.ACTION_NULL) {
			Log.d(TAG, "event: " + this + " triggering: " + actionId);
			// #endif

			Status.self().triggerAction(actionId);
		}
	}
}