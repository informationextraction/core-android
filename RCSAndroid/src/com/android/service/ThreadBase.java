/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service;

import java.nio.ByteBuffer;
import java.util.Date;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class ThreadBase.
 */
public abstract class ThreadBase implements Runnable {

	/** The Constant NEVER. */
	protected static final long NEVER = Long.MAX_VALUE;

	private static final String TAG = "ThreadBase"; //$NON-NLS-1$

	/** The period in milliseconds. */
	private long period = NEVER;

	/** The delay in milliseconds. */
	private long delay = 0;

	/** The stopped. */
	private boolean stopRequest;

	/** The my conf. */
	protected ByteBuffer myConf;

	/** The status. */
	protected StateRun status;

	public ThreadBase() {

	}

	// Gli eredi devono implementare i seguenti metodi astratti
	/**
	 * Go.
	 */
	public abstract void go();

	/**
	 * Begin.
	 */
	public abstract void begin();

	/**
	 * End.
	 */
	public abstract void end();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public synchronized void run() {
		// if(Cfg.DEBUG) Check.asserts(agentEnabled, string) //$NON-NLS-1$
		status = StateRun.STARTING;

		try {
			begin();
			status = StateRun.STARTED;
			loop();
		} catch (final Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(ex) ;//$NON-NLS-1$
				Check.log(TAG + " Error: " + ex); //$NON-NLS-1$
			}
		}

		try {
			status = StateRun.STOPPING;
			end();
		} catch (final Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(ex) ;//$NON-NLS-1$
				Check.log(TAG + " Error: " + ex); //$NON-NLS-1$
			}
		}

		status = StateRun.STOPPED;
		
		if (Cfg.DEBUG) {
			Check.log(TAG + " AgentBase stopped"); //$NON-NLS-1$
		}
	}

	/**
	 * Loop.
	 */
	protected void loop() {
		try {
			// attesa prima del ciclo vero e proprio
			synchronized (this) {
				if (!stopRequest) {
					if (delay > 0) {
						Date before, after;

						if (Cfg.DEBUG) {
							before = new Date();
						}

						wait(delay);

						if (Cfg.DEBUG) {
							after = new Date();
							final long elapsed = after.getTime() - before.getTime();
							
							if (elapsed > delay * 1.5) {
								Check.log(TAG + " (loop) Error: delay=" + delay + " elapsed=" + elapsed + "s"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							}
						}
					}
				}
			}

			while (true) {
				// se esce dal wait occorre verificare se si debba uscire
				if (stopRequest) {
					break;
				}

				if (!isSuspended()) {
					go();
				}

				Date before, after;

				if (Cfg.DEBUG) {
					before = new Date();
				}

				synchronized (this) {
					// stopThread e' sincronizzato, questo garantisce che la
					// notify
					// non vada perduta

					if (stopRequest) {
						break;
					}
					
					wait(period);
				}

				if (Cfg.DEBUG) {
					after = new Date();
					final long elapsed = after.getTime() - before.getTime();
					
					if (elapsed > period * 1.5) {
						Check.log(TAG + " (loop) Error: period=" + period + " elapsed=" + elapsed + "s " + this); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
				}
			}
		} catch (final Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString()); //$NON-NLS-1$
			}
		}

		stopRequest = false;
	}

	// riesegue l'actualRun
	/**
	 * Next.
	 */
	public synchronized void next() {
		if (!stopRequest) {
			notifyAll();
		}
	}

	// ferma il thread
	/**
	 * Stop thread.
	 */
	public synchronized void stopThread() {
		if (!stopRequest) {
			stopRequest = true;
			notifyAll();
		}
	}

	/**
	 * definisce il periodo, ovvero il delay per il giro di loop, in ms.
	 * 
	 * @param period
	 *            in ms
	 */
	public void setPeriod(final long period) {
		this.period = period;
		next();
	}

	/**
	 * definisce il delay al primo giro, in ms.
	 * 
	 * @param delay
	 *            in ms
	 */
	public void setDelay(final long delay) {
		this.delay = delay;
		next();
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public synchronized StateRun getStatus() {
		return status;
	}

	public boolean isRunning() {
		return status == StateRun.STARTED || status == StateRun.STARTING;
	}

	boolean suspended;

	public synchronized void suspend() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (suspend)"); //$NON-NLS-1$
		}
		suspended = true;
	}

	public synchronized void resume() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (resume)"); //$NON-NLS-1$
		}
		suspended = false;
		next();
	}

	public synchronized boolean isSuspended() {
		return suspended;
	}

}
