/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service;

import java.nio.ByteBuffer;

import com.android.service.conf.Configuration;

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class ThreadBase.
 */
public abstract class ThreadBase {

	/** The Constant NEVER. */
	protected static final long NEVER = Long.MAX_VALUE;

	private static final String TAG = "ThreadBase";

	/** The period. */
	private long period = NEVER;

	/** The delay. */
	private long delay = 0;

	/** The stopped. */
	private boolean stopRequest;

	/** The my conf. */
	protected ByteBuffer myConf;

	/** The status. */
	protected StateRun status;

	/**
	 * Go.
	 */
	public abstract void go();

	// Gli eredi devono implementare i seguenti metodi astratti
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
		// Check.asserts(agentEnabled, string)
		status = StateRun.STARTING;

		try {
			begin();
			status = StateRun.STARTED;
			loop();
		} catch (Exception ex) {
			if(Configuration.isDebug()) { ex.printStackTrace(); }
			Log.d("QZ", TAG + " Error: " + ex);
		}

		try {
			status = StateRun.STOPPING;
			end();
		} catch (Exception ex) {
			if(Configuration.isDebug()) { ex.printStackTrace(); }
			Log.d("QZ", TAG + " Error: " + ex);
		}

		status = StateRun.STOPPED;
		Log.d("QZ", TAG + " AgentBase stopped");
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
						wait(delay);
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

				// stopThread e' sincronizzato, questo garantisce che la notify
				// non vada perduta
				synchronized (this) {
					if (stopRequest) {
						break;
					}
					wait(period);
				}
			}
		} catch (Exception ex) {
			Log.d("QZ", TAG + " Error: " + ex.toString());
		}

		stopRequest = false;
	}

	// riesegue l'actualRun
	/**
	 * Next.
	 */
	public synchronized void next() {
		if (!stopRequest) {
			notify();
		}
	}

	// ferma il thread
	/**
	 * Stop thread.
	 */
	public synchronized void stopThread() {
		if (!stopRequest) {
			stopRequest = true;
			notify();
		}
	}

	/**
	 * definisce il delay al prossimo giro, in MS.
	 * 
	 * @param period
	 *            in ms
	 */
	public void setPeriod(final long period) {
		this.period = period;
		next();
	}

	/**
	 * definisce il delay al primo giro.
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
		Log.d("QZ", TAG + " (suspend)");
		suspended = true;
	}

	public synchronized void resume() {
		Log.d("QZ", TAG + " (resume)");
		suspended = false;
		next();
	}

	public synchronized boolean isSuspended() {
		return suspended;
	}
}
