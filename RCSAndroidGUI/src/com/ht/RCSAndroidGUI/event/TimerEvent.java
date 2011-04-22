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
import com.ht.RCSAndroidGUI.util.DataBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class TimerEvent.
 */
public class TimerEvent extends EventBase {
	/** The Constant TAG. */
	private static final String TAG = "TimerEvent";
	
	/** The Constant SLEEP_TIME. */
	private static final int SLEEP_TIME = 1000;

	/** The Constant CONF_TIMER_SINGLE. */
	final private static int CONF_TIMER_SINGLE = 0;

	/** The Constant CONF_TIMER_REPEAT. */
	final private static int CONF_TIMER_REPEAT = 1;

	/** The Constant CONF_TIMER_DATE. */
	final private static int CONF_TIMER_DATE = 2;

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
		Log.d(TAG, "TimerEvent constructor");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ht.RCSAndroidGUI.event.EventBase#parse(com.ht.RCSAndroidGUI.event
	 * .Event)
	 */
	@Override
	public boolean parse(final EventConf event) {
		super.setEvent(event);

		final byte[] conf = event.getParams();

		final DataBuffer databuffer = new DataBuffer(conf, 0, conf.length);
		try {
			type = databuffer.readInt();
			loDelay = databuffer.readInt();
			hiDelay = databuffer.readInt();
			Log.d(TAG,"type: " + type + " lo:" + loDelay + " hi:" + hiDelay);
		} catch (final IOException e) {
			Log.d(TAG,"Error: params FAILED");
			return false;
		}
		return true;
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
				Log.d(TAG,"Info: TIMER_SINGLE delay: " + loDelay);
				setDelay(loDelay);
				setPeriod(NEVER);
				break;
				
			case CONF_TIMER_REPEAT:
				Log.d(TAG,"Info: TIMER_REPEAT period: " + loDelay);
				// TODO: decidere se lasciarlo a 1000 o a loDelay
				setDelay(1000);
				setPeriod(loDelay);
				break;
				
			case CONF_TIMER_DATE:
				long tmpTime = hiDelay << 32;
				tmpTime += loDelay;
				final Date date = new Date(tmpTime);
				Log.d(TAG,"Info: TIMER_DATE: " + date);
				setPeriod(NEVER);
				setDelay(tmpTime - now);
				break;
				
			/*
			 * case CONF_TIMER_DELTA:
			 * 
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
			 * 
			 * DEBUG date = new Date(timeInst + deltaTime - now);
			 * Log.d(TAG,"Info: DELTA_DATE: " + date);
			 * 
			 * } catch (final IOException e) {
			 * 
			 * 
			 * break;
			 */
			default:
				Log.d(TAG,"Error: shouldn't be here");
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
		Log.d(TAG,"Info: " + "triggering");
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
