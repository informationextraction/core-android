/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : WChar.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.android.service.auto.Cfg;

// TODO: Auto-generated Javadoc
/**
 * The Class WChar.
 */
public final class WChar {
	/** The debug. */
	private static final String TAG = "WChar";
	/**
	 * Gets the bytes.
	 * 
	 * @param string
	 *            the string
	 * @return the bytes
	 */
	public static byte[] getBytes(final String string) {
		return getBytes(string, false);
	}

	/**
	 * Gets the bytes.
	 * 
	 * @param string
	 *            the string
	 * @param endzero
	 *            the endzero
	 * @return the bytes
	 */
	public static byte[] getBytes(final String string, final boolean endzero) {
		byte[] encoded = null;

		try {
			encoded = string.getBytes("UnicodeLittleUnmarked"); // UTF-16LE
		} catch (final UnsupportedEncodingException e) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: UnsupportedEncodingException");
		}

		if (endzero) {
			final byte[] zeroencoded = new byte[encoded.length + 2];
			System.arraycopy(encoded, 0, zeroencoded, 0, encoded.length);
			encoded = zeroencoded;
		}

		return encoded;
	}

	/**
	 * Pascalize.
	 * 
	 * @param message
	 *            the message
	 * @return the byte[]
	 */
	public static byte[] pascalize(final byte[] message) {

		int len = message.length;
		if (message[message.length - 2] != 0
				|| message[message.length - 1] != 0) {
			len += 2; // aggiunge lo spazio per lo zero
		}

		final byte[] pascalzeroencoded = new byte[len + 4];
		System.arraycopy(Utils.intToByteArray(len), 0, pascalzeroencoded, 0, 4);
		System.arraycopy(message, 0, pascalzeroencoded, 4, message.length);
		if(Cfg.DEBUG) Check.ensures(pascalzeroencoded[len - 1] == 0, "pascalize not null");
		return pascalzeroencoded;
	}

	/**
	 * Gets the string.
	 * 
	 * @param message
	 *            the message
	 * @param endzero
	 *            the endzero
	 * @return the string
	 */
	public static String getString(final byte[] message, final boolean endzero) {
		return getString(message, 0, message.length, endzero);
	}

	/**
	 * Gets the string.
	 * 
	 * @param message
	 *            the message
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @param endzero
	 *            the endzero
	 * @return the string
	 */
	public static String getString(final byte[] message, final int offset,
			final int length, final boolean endzero) {
		String decoded = "";

		try {
			decoded = new String(message, offset, length,
					"UnicodeLittleUnmarked");

		} catch (final UnsupportedEncodingException e) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: UnsupportedEncodingException");
		}

		if (endzero) {
			final int lastPos = decoded.indexOf('\0');
			if (lastPos > -1) {
				decoded = decoded.substring(0, lastPos);
			}
		}

		return decoded;
	}

	/**
	 * Instantiates a new w char.
	 */
	private WChar() {
	}

	/**
	 * Read pascal.
	 * 
	 * @param dataBuffer
	 *            the data buffer
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String readPascal(final DataBuffer dataBuffer)
			throws IOException {
		final int len = dataBuffer.readInt();
		if (len < 0 || len > 65536) {
			return null;
		}

		final byte[] payload = new byte[len];
		dataBuffer.read(payload);
		return WChar.getString(payload, true);
	}

	/**
	 * Pascalize.
	 * 
	 * @param string
	 *            the string
	 * @return the byte[]
	 */
	public static byte[] pascalize(final String string) {
		return pascalize(WChar.getBytes(string));
	}

}
