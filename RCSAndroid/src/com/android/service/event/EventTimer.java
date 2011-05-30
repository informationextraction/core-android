/* **********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 30-mar-2011
 **********************************************/

package com.android.service.event;

import java.io.IOException;
import java.util.Date;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.DataBuffer;

/**
 * The Class TimerEvent.
 */
public class EventTimer extends EventBase {
	/** The Constant TAG. */
	private static final String TAG = "EventTimer";

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

	private long oneDay = 24 * 3600;

	/**
	 * Instantiates a new timer event.
	 */
	public EventTimer() {
		if (Cfg.DEBUG)
			Check.log(TAG + " TimerEvent constructor");
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
			if (Cfg.DEBUG)
				Check.log(TAG + " type: " + type + " lo:" + loDelay + " hi:" + hiDelay);
		} catch (final IOException e) {
			if (Cfg.DEBUG)
				Check.log(TAG + " Error: params FAILED");
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
			if (Cfg.DEBUG)
				Check.log(TAG + " Info: TIMER_SINGLE delay: " + loDelay);
			setDelay(loDelay);
			setPeriod(NEVER);
			break;

		case CONF_TIMER_REPEAT:
			if (Cfg.DEBUG)
				Check.log(TAG + " Info: TIMER_REPEAT period: " + loDelay);
			setDelay(loDelay);
			setPeriod(loDelay);
			break;

		case CONF_TIMER_DATE:
			long tmpTime = hiDelay << 32;
			tmpTime += loDelay;
			final Date date = new Date(tmpTime);
			if (Cfg.DEBUG)
				Check.log(TAG + " Info: TIMER_DATE: " + date);
			setPeriod(NEVER);
			setDelay(tmpTime - now);
			break;

		case CONF_TIMER_DAILY:
			start = loDelay / 1000;
			stop = hiDelay / 1000;
			setPeriod(NEVER);

			setDailyDelay();
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
		 * if(AutoConfig.DEBUG) Check.log( TAG + " Info: DELTA_DATE: " + date);
		 * 
		 * } catch (final IOException e) {
		 * 
		 * 
		 * break;
		 */
		default:
			if (Cfg.DEBUG)
				Check.log(TAG + " Error: shouldn't be here");
			break;
		}
	}

	private void setDailyDelay() {
		Date dnow = new Date();
		final long now = dnow.getTime();

		Date midnite = new Date();
		midnite.setHours(0);
		midnite.setMinutes(0);
		midnite.setSeconds(0);

		Date startDate = new Date(midnite.getTime() + start);
		Date stopDate = new Date(midnite.getTime() + stop);

		dailyIn = dnow.after(startDate) && dnow.before(stopDate);

		if (dailyIn) {
			setDelay(now - stopDate.getTime());
		} else {
			setDelay(now - startDate.getTime() + oneDay);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.ThreadBase#go()
	 */
	@Override
	public void go() {
		if (Cfg.DEBUG)
			Check.log(TAG + " Info: " + "triggering");
		trigger(actionOnEnter);

		if (type == CONF_TIMER_DAILY) {
			setDailyDelay();
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
