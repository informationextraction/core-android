/***********************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 30-mar-2011
 **********************************************/

package com.ht.RCSAndroidGUI.event;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Date;

import com.ht.RCSAndroidGUI.Debug;
import com.ht.RCSAndroidGUI.utils.DataBuffer;
import com.ht.RCSAndroidGUI.utils.Utils;

import android.util.Log;

public class TimerEvent extends EventBase {
	private static final int SLEEP_TIME = 1000;

	// #ifdef DEBUG
	private static Debug debug = new Debug("TimerEvent");
	// #endif

	final private static int CONF_TIMER_SINGLE = 0;
	final private static int CONF_TIMER_REPEAT = 1;
	final private static int CONF_TIMER_DATE = 2;

	private int type;
	long loDelay;
	long hiDelay;

	public TimerEvent() {
		Log.d("RCS", "TimerEvent constructor");
	}

	public void parse(Event event) {
		super.setEvent(event);

		byte[] conf = event.getParams();

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
			setDelay(loDelay);
			setPeriod(loDelay);
			break;
		case CONF_TIMER_DATE:
			long tmpTime = hiDelay << 32;
			tmpTime += loDelay;
			// #ifdef DEBUG
			Date date = new Date(tmpTime);
			debug.info("TIMER_DATE: " + date);
			// #endif

			setPeriod(NEVER);
			setDelay(tmpTime - now);
			break;
		/*case CONF_TIMER_DELTA:
			// #ifdef DEBUG
			debug.info("TIMER_DELTA");
			// #endif

			long deltaTime = hiDelay << 32;
			deltaTime += loDelay;

			// se la data di installazione non c'e' si crea.
			if (!markup.isMarkup()) {
				final Date instTime = Status.getInstance().getStartingDate();
				markup.writeMarkup(Utils.longToByteArray(instTime.getTime()));
			}

			// si legge la data di installazione dal markup
			try {
				final long timeInst = Utils.byteArrayToLong(
						markup.readMarkup(), 0);

				setPeriod(NEVER);
				final long delay = timeInst + deltaTime - now;
				if (delay > 0) {
					setDelay(timeInst + deltaTime - now);
				} else {
					// #ifdef DEBUG
					debug.info("negative delta");
					// #endif
				}
				// #ifdef DEBUG
				date = new Date(timeInst + deltaTime - now);
				debug.info("DELTA_DATE: " + date);
				// #endif

			} catch (final IOException e) {
				// #ifdef ERROR
				debug.error(e);
				// #endif
			}

			break;*/
		default:
			// #ifdef DEBUG
			debug.error("shouldn't be here");
			// #endif
			break;
		}
	}

	public void go() {
		trigger();
	}

	public void end() {

	}
}
