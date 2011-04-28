/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI;

import java.nio.ByteBuffer;

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
	private boolean stopped;
	
	/** The my conf. */
	protected ByteBuffer myConf;

	/** The status. */
	protected int status;

	/**
	 * Go.
	 */
	public abstract void go();

	/**
	 * Loop.
	 */
	protected void loop() {
		try {
			wait(delay);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		while (true) {
			synchronized(this){
				if (stopped) {
					break;
				}
			}
			try {
				go();

				try {
					wait(period);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception ex) {
				Log.d("QZ", TAG + " Error: " + ex.toString());
			}
			
			Log.d("QZ", TAG + " ThreadBase Running");
		}
	}

	// riesegue l'actualRun
	/**
	 * Next.
	 */
	public synchronized void next() {
		if (!stopped) {
			notify();
		}
	}

	// ferma il thread
	/**
	 * Stop thread.
	 */
	public synchronized void stopThread() {
		if (!stopped) {
			stopped = true;
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
	public synchronized int getStatus() {
		return status;
	}

}
