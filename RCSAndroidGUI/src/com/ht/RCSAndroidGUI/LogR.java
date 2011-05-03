/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : LogR.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.evidence.EvidenceType;
import com.ht.RCSAndroidGUI.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class LogR.
 */
public class LogR {

	/** The type. */
	private final EvidenceType type;

	/** The unique. */
	private final long unique;

	/** The disp. */
	private LogDispatcher disp;

	/** The Constant LOG_CREATE. */
	final public static int LOG_CREATE = 0x1;

	/** The Constant LOG_ATOMIC. */
	final public static int LOG_ATOMIC = 0x2;

	/** The Constant LOG_APPEND. */
	final public static int LOG_APPEND = 0x3;

	/** The Constant LOG_WRITE. */
	final public static int LOG_WRITE = 0x4;

	/** The Constant LOG_CLOSE. */
	final public static int LOG_CLOSE = 0x5;

	/** The Constant LOG_REMOVE. */
	final public static int LOG_REMOVE = 0x6;

	/** The Constant LOG_REMOVEALL. */
	final public static int LOG_REMOVEALL = 0x7;

	/** The Constant LOG_WRITEMRK. */
	final public static int LOG_WRITEMRK = 0x8;

	/** The Constant LOG_PRI_MAX. */
	final public static int LOG_PRI_MAX = 0x1;

	/** The Constant LOG_PRI_STD. */
	final public static int LOG_PRI_STD = 0x7f;

	/** The Constant LOG_PRI_MIN. */
	final public static int LOG_PRI_MIN = 0xff;

	/**
	 * Instantiates a new log, creates the evidence.
	 * 
	 * @param evidence
	 *            the log type
	 * @param priority
	 *            the priority
	 */
	public LogR(final EvidenceType evidence, final int priority) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidence;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);

		send(p);
	}

	/**
	 * Instantiates a new log, creates the evidence with additional.
	 * 
	 * @param evidenceType
	 *            the log type
	 * @param priority
	 *            the priority
	 * @param additional
	 *            the additional
	 */
	public LogR(final EvidenceType evidenceType, final int priority, final byte[] additional) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidenceType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);
		p.setAdditional(additional);

		send(p);
	}

	/**
	 * Instantiates a new log, creates atomically the evidence with additional
	 * and data.
	 *
	 * @param evidenceType the log type
	 * @param priority the priority
	 * @param additional the additional
	 * @param data the data
	 */
	public LogR(final EvidenceType evidenceType, final int priority, final byte[] additional,
			final byte[] data) {
		unique = Utils.getRandom();
		disp = LogDispatcher.self();
		type = evidenceType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_ATOMIC);
		p.setAdditional(additional);
		p.fill(data);

		send(p);
	}
	
	public LogR(final EvidenceType evidenceType,  final byte[] additional,
			final byte[] data) {
		this(evidenceType, LOG_PRI_STD, additional, data);
	}

	// Send data to dispatcher
	/**
	 * Send.
	 * 
	 * @param p
	 *            the p
	 */
	private void send(final Packet p) {
		if (disp == null) {
			disp = LogDispatcher.self();

			if (disp == null) {
				return;
			}
		}

		disp.send(p);
	}

	/**
	 * Write or append data to the log.
	 * 
	 * @param data
	 *            the data
	 */
	public void write(final byte[] data) {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_WRITE);
		p.fill(data);

		send(p);
		return;
	}

	/**
	 * Write markup.
	 * 
	 * @param data
	 *            the data
	 */
	public void writeMarkup(final byte[] data) {
		final Packet p = new Packet(unique);

		p.setType(type);
		p.setCommand(LOG_WRITEMRK);
		p.fill(data);

		send(p);
		return;
	}

	/**
	 * Close.
	 */
	public void close() {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_CLOSE);

		send(p);
		return;
	}
}
