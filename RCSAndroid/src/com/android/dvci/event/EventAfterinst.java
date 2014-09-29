package com.android.dvci.event;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.android.dvci.conf.ConfEvent;
import com.android.dvci.conf.ConfigurationException;
import com.android.dvci.evidence.Markup;

public class EventAfterinst extends BaseTimer {

	private int days;
	private Date date;

	@Override
	protected boolean parse(ConfEvent conf) {
		try {
			days = conf.getInt("days");
			Markup markup = new Markup(this);
			Date now = new Date();

			if (markup.isMarkup()) {
				date = (Date) markup.readMarkupSerializable();
			} else {
				date = now;
				markup.writeMarkupSerializable(date);
			}

		} catch (ConfigurationException e) {

			return false;
		} catch (IOException e) {

			return false;
		}
		return true;
	}

	@Override
	protected void actualStart() {
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);
		long nowMillis = calendar.getTimeInMillis();

		calendar.add(Calendar.DAY_OF_MONTH, days);
		long triggerMillis = calendar.getTimeInMillis();

		long delay = triggerMillis - nowMillis;

		if (delay > 0) {
			setDelay(delay);
			setPeriod(NEVER);
		} else {
			setDelay(SOON);
		}
	}

	@Override
	protected void actualStop() {
		onExit(); // di sicurezza
	}

	@Override
	protected void actualGo() {
		onEnter();
	}
}
