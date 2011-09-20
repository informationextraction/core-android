/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

/**
 * The Class TimerEvent.
 */
public class EventTimer extends EventBase {
	/** The Constant TAG. */
	private static final String TAG = "EventTimer"; //$NON-NLS-1$

	/** The Constant SLEEP_TIME. */
	private static final int SLEEP_TIME = 1000;

	/** The Constant CONF_TIMER_SINGLE. */
	final private static int CONF_TIMER_SINGLE = 0;

	/** The Constant CONF_TIMER_REPEAT. */
	final private static int CONF_TIMER_REPEAT = 1;

	/** The Constant CONF_TIMER_DATE. */
	final private static int CONF_TIMER_DATE = 2;

	final private static int CONF_TIMER_DELTA = 3;

	final private static int CONF_TIMER_DAILY = 4;

	private int actionOnEnter, actionOnExit;

	boolean dailyIn;

	/** The type. */
	private int type;

	/** The lo delay. */
	long loDelay;

	/** The hi delay. */
	long hiDelay;

	long start, stop;

	private final long oneDayMs = 24 * 3600 * 1000;

	/**
	 * Instantiates a new timer event.
	 */
	public EventTimer() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " TimerEvent constructor");//$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ht.AndroidServiceGUI.event.EventBase#parse(com.ht.AndroidServiceGUI
	 * .event .Event)
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
			
			actionOnEnter = event.getAction();
			actionOnExit = databuffer.readInt();

			if (Cfg.DEBUG) {
				Check.log(TAG + " type: " + type + " lo:" + loDelay + " hi:" + hiDelay);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED");//$NON-NLS-1$
			}

			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.event.EventBase#begin()
	 */
	@Override
	public void begin() {
		final long now = System.currentTimeMillis();

		switch (type) {
		case CONF_TIMER_SINGLE:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: TIMER_SINGLE delay: " + loDelay);//$NON-NLS-1$
			}

			setDelay(loDelay);
			setPeriod(NEVER);
			break;

		case CONF_TIMER_REPEAT:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: TIMER_REPEAT period: " + loDelay);//$NON-NLS-1$
			}

			setDelay(loDelay);
			setPeriod(loDelay);
			break;

		case CONF_TIMER_DATE:
			long tmpTime = hiDelay << 32;
			tmpTime += loDelay;
			final Date date = new Date(tmpTime);

			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: TIMER_DATE: " + date);//$NON-NLS-1$
			}

			setPeriod(NEVER);
			setDelay(tmpTime - now);
			break;

		case CONF_TIMER_DAILY:
			start = loDelay;
			stop = hiDelay;
			setPeriod(NEVER);

			dailyIn = setDailyDelay();
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
		 * if(AutoConfig.DEBUG) Check.log( TAG + " Info: DELTA_DATE: " + date)
		 * ;//$NON-NLS-1$
		 * 
		 * } catch (final IOException e) {
		 * 
		 * 
		 * break;
		 */

		default:
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: shouldn't be here");//$NON-NLS-1$
			}

			break;
		}
	}

	private boolean setDailyDelay() {
		Calendar nowCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		long nextStart, nextStop;
		
		int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600) + (nowCalendar.get(Calendar.MINUTE) * 60) 
							+ nowCalendar.get(Calendar.SECOND)) * 1000;

		// Estriamo il prossimo evento e determiniamo il delay sulla base del tipo
		if (start > now)
			nextStart = start;
		else
			nextStart = start + (3600 * 24 * 1000); // 1 Day

		if (stop > now)
			nextStop = stop;
		else
			nextStop = stop + (3600 * 24 * 1000); // 1 Day

		if (nextStop > nextStart) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): Delay (next start): " + (nextStart - now)); //$NON-NLS-1$
			}
			setPeriod(nextStart - now);
			return true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): Delay (next stop): " + (nextStop - now)); //$NON-NLS-1$
			}
			
			setPeriod(nextStop - now);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "triggering");//$NON-NLS-1$ //$NON-NLS-2$
		}

		if (type == CONF_TIMER_DAILY) {
			if (dailyIn) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): DAILY TIMER: action enter"); //$NON-NLS-1$
				}
				trigger(actionOnEnter);
			} else {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (go): DAILY TIMER: action exit"); //$NON-NLS-1$
				}
				trigger(actionOnExit);
			}
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): daily IN BEFORE: " + dailyIn); //$NON-NLS-1$
			}
			dailyIn = setDailyDelay();
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): daily IN AFTER: " + dailyIn); //$NON-NLS-1$
			}
		} else {
			trigger(actionOnEnter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.event.EventBase#end()
	 */
	@Override
	public void end() {

	}
}
