package com.android.networking.event;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.networking.auto.Cfg;
import com.android.networking.conf.ConfEvent;
import com.android.networking.conf.ConfigurationException;
import com.android.networking.util.Check;

public class EventDate extends BaseTimer {
	private static final String TAG = "EventDate";

	private Date dateFrom;
	private Date dateTo;

	Calendar start;
	Calendar stop;

	private boolean nextDailyIn = false;
	private boolean needExitOnStop = false;

	@Override
	protected boolean parse(ConfEvent event) {
		needExitOnStop = false;

		try {
			dateFrom = conf.getDate("datefrom");
			if (conf.has("dateto")) {
				dateTo = conf.getDate("dateto");
			} else {
				dateTo = new Date(Long.MAX_VALUE);
			}
		} catch (ConfigurationException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return false;
		} 

		return true;
	}

	@Override
	public void actualStart() {
		start = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		start.setTime(dateFrom);
		stop = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		stop.setTime(dateTo);

		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		if (now.before(start)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): not yet in the brackets");
			}

			nextDailyIn = setDailyDelay(true);
		} else if (now.before(stop)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): already in the brackets");
			}

			nextDailyIn = setDailyDelay(true);
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): nothing to do");
			}
		}
	}

	private boolean setDailyDelay(boolean isDelay) {
		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

		if (now.before(start)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay start in ms): " + (start.getTimeInMillis() - now.getTimeInMillis()));
			}

			if (isDelay) {
				setDelay((start.getTimeInMillis() - now.getTimeInMillis()));
			} else {
				setPeriod((start.getTimeInMillis() - now.getTimeInMillis()));
			}

			return true;
		} else if (now.before(stop)) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (setDailyDelay stop in ms): nothing to do"
						+ (stop.getTimeInMillis() - now.getTimeInMillis()));
			}

			if (isDelay) {
				setDelay((stop.getTimeInMillis() - now.getTimeInMillis()));
			} else {
				setPeriod((stop.getTimeInMillis() - now.getTimeInMillis()));
			}

			return false;
		} else {
			this.onExit();

			setPeriod(NEVER);
			setDelay(NEVER);

			return false;
		}
	}

	@Override
	public void actualGo() {
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

		nextDailyIn = setDailyDelay(false);
	}

	@Override
	public void actualStop() {
		if (needExitOnStop) {
			onExit(); // di sicurezza
		}
	}

}
