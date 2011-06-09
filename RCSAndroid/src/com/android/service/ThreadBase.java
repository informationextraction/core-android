/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

/**
 * The Class ThreadBase.
 */
public abstract class ThreadBase implements Runnable {

	/** The Constant NEVER. */
	protected static final long NEVER = Long.MAX_VALUE;

	private static final String TAG = "ThreadBase";

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
		// if(Cfg.DEBUG) Check.asserts(agentEnabled, string)
		status = StateRun.STARTING;

		try {
			begin();
			status = StateRun.STARTED;
			loop();
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(ex);
				Check.log(TAG + " Error: " + ex);
			}

		}

		try {
			status = StateRun.STOPPING;
			end();
		} catch (Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(ex);
				Check.log(TAG + " Error: " + ex);
			}

		}

		status = StateRun.STOPPED;
		if (Cfg.DEBUG)
			Check.log(TAG + " AgentBase stopped");
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

						if (Cfg.DEBUG)
							before = new Date();

						wait(delay);

						if (Cfg.DEBUG) {
							after = new Date();
							long elapsed = after.getTime() - before.getTime();
							if (elapsed > delay * 1.5) {
								Log.d("QZ", TAG + " (loop) Error: delay=" + delay + " elapsed=" + elapsed + "s");
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

				if (Cfg.DEBUG)
					before = new Date();

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
					long elapsed = after.getTime() - before.getTime();
					if (elapsed > period * 1.5) {
						Log.d("QZ", TAG + " (loop) Error: period=" + period + " elapsed=" + elapsed + "s " + this);
					}
				}

			}
		} catch (Exception ex) {
			if (Cfg.DEBUG)
				Check.log(TAG + " Error: " + ex.toString());
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
		if (Cfg.DEBUG)
			Check.log(TAG + " (suspend)");
		suspended = true;
	}

	public synchronized void resume() {
		if (Cfg.DEBUG)
			Check.log(TAG + " (resume)");
		suspended = false;
		next();
	}

	public synchronized boolean isSuspended() {
		return suspended;
	}

}
