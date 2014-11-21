/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.dvci;

import java.util.Date;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;

/**
 * The Class ThreadBase.
 */
public abstract class ThreadBase implements Runnable {

	/** The Constant NEVER. */
	protected static final long NEVER = Long.MAX_VALUE;
	/** The Constant NEVER. */
	protected static final long SOON = 0;

	private static final String TAG = "ThreadBase"; //$NON-NLS-1$

	/** The period in milliseconds. */
	private long period = NEVER;

	/** The delay in milliseconds. */
	private long delay = 0;

	/** The stopped. */
	private boolean stopRequest;

	private boolean suspended;
	/** The status. */
	protected StateRun status;

	public ThreadBase() {
		if (Cfg.DEBUG) {
			Check.log(TAG +" sub-class:"+getTag()+ " (new) suspended="+suspended  ); //$NON-NLS-1$
		}
	}

	// Gli eredi devono implementare i seguenti metodi astratti
	/**
	 * Go. Viene lanciato dopo il delay, ogni period.
	 */
	protected abstract void actualGo();

	/**
	 * Begin. Viene lanciato quando il servizio viene creato. Se vuole puo'
	 * definire il delay e il period.
	 */
	protected abstract void actualStart();

	/**
	 * End. Viene invocato quando il servizio viene chiuso.
	 */
	protected abstract void actualStop();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public synchronized void run() {
		// if(Cfg.DEBUG) Check.asserts(agentEnabled, string) //$NON-NLS-1$
		status = StateRun.STARTING;

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (run) starting: " + this);
			}

			actualStart();
			status = StateRun.STARTED;
			loop();
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
				Check.log(TAG + " Error: " + ex); //$NON-NLS-1$
			}
		}

		try {
			status = StateRun.STOPPING;

			if (Cfg.DEBUG) {
				Check.log(TAG + " (run) stopping: " + this);
			}

			actualStop();
		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(ex);//$NON-NLS-1$
				Check.log(TAG + " Error: " + ex); //$NON-NLS-1$
			}
		}

		status = StateRun.STOPPED;

		if (Cfg.DEBUG) {
			Check.log(TAG + " AgentBase stopped"); //$NON-NLS-1$
		}
	}

	/**
	 * Loop. I synchronized qui dentro forse non servono, perche' questo metodo
	 * e' chiamato solo da run, che e' gia' sincronizzato
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
					actualGo();
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
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(ex);
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
	
	public boolean isStopRequested(){
		return stopRequest;
	}

	/**
	 * definisce il periodo, ovvero il delay per il giro di loop, in ms.
	 * 
	 * @param period
	 *            in ms
	 */
	protected void setPeriod(final long period) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (setPeriod) ");
		}
		this.period = period;
		next();
	}

	/**
	 * definisce il delay al primo giro, in ms.
	 * 
	 * @param delay
	 *            in ms
	 */
	protected void setDelay(final long delay) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (setDelay) " + delay);
		}
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
		return (status == StateRun.STARTED || status == StateRun.STARTING);
	}

	public synchronized void suspend() {
		suspended = true;
		if (Cfg.DEBUG) {
			Check.log(TAG + "hash:"+this.hashCode()+" sub-class:"+getTag()+ " (suspend) suspended="+suspended  ); //$NON-NLS-1$
		}
	}

	public synchronized void resume() {
		suspended = false;
		if (Cfg.DEBUG) {
			Check.log(TAG + "hash:"+this.hashCode()+" sub-class:"+getTag()+" (resume) suspended="+suspended ); //$NON-NLS-1$
		}
		next();
	}

	public synchronized boolean isSuspended() {
		if (Cfg.DEBUG) {
			Check.log(TAG + "hash:"+this.hashCode()+ " sub-class:"+getTag()+" (isSuspended) suspended="+suspended ); //$NON-NLS-1$
		}
		return suspended;
	}
	public String getTag(){
		return TAG;
	}

}
