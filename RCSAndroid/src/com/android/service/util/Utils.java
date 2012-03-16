/* ******************************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : AndroidService
 * Created   : 01-dec-2010
 *******************************************************/

package com.android.service.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import com.android.service.auto.Cfg;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public final class Utils {

	/** The debug. */
	private static final String TAG = "Utils"; //$NON-NLS-1$

	private Utils() {
	};

	/**
	 * Converts a Buffer to a DataInputStream.
	 * 
	 * @param buffer
	 *            : Input buffer
	 * @return DataInputStream, must be closed by the caller.
	 */
	static final DataInputStream BufferToDataInputStream(final byte[] buffer) {
		final ByteArrayInputStream bufferByteStream = new ByteArrayInputStream(buffer);
		final DataInputStream bufferDataStream = new DataInputStream(bufferByteStream);

		try {
			bufferByteStream.close();
		} catch (final IOException ioe) {
			if (Cfg.EXCEPTION) {
				Check.log(ioe);
			}

			if (Cfg.DEBUG) {
				Check.log(ioe);//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(TAG + " IOException() caught in Utils.BufferToDataInputStream()");//$NON-NLS-1$
			}
		}

		return bufferDataStream;
	}

	/**
	 * Converts a Buffer to a ByteBuffer.
	 * 
	 * @param buffer
	 *            : Input buffer
	 * @param order
	 *            : ByteOrder, LITTLE_ENDIAN or BIG_ENDIAN
	 * @return ByteBuffer.
	 */
	public static final ByteBuffer bufferToByteBuffer(final byte[] buffer, final ByteOrder order) {
		final ByteBuffer retBuff = ByteBuffer.wrap(buffer);
		retBuff.order(order);

		return retBuff;
	}

	/**
	 * Converts a Buffer to a ByteBuffer with boundary constraints.
	 * 
	 * @param buffer
	 *            : Input buffer
	 * @param order
	 *            : ByteOrder, LITTLE_ENDIAN or BIG_ENDIAN
	 * @param start
	 *            : Offset from which to start the buffer creation
	 * @param len
	 *            : Length at which the conversion will stop
	 * @return ByteBuffer.
	 */
	static final ByteBuffer BufferToByteBuffer(final byte[] buffer, final ByteOrder order, final int start,
			final int len) {
		final ByteBuffer retBuff = ByteBuffer.wrap(buffer, start, len);
		retBuff.order(order);

		return retBuff;
	}

	/**
	 * Converts an InputStream into a buffer.
	 * 
	 * @param iStream
	 *            : InputStream that will be converted
	 * @param offset
	 *            : Used to discard _offset_ bytes from the resource
	 * @return byte[], an array filled with data from InpustrStream.
	 */
	public static final byte[] inputStreamToBuffer(final InputStream iStream, final int offset) {
		try {
			int i;

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);

			byte[] buffer = new byte[1024];

			if (offset > 0) {
				byte[] discard = new byte[offset];
				iStream.read(discard);
				discard = null;
			}

			while ((i = iStream.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, i);
			}

			iStream.close();

			return byteArrayOutputStream.toByteArray();
		} catch (final IOException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " IOException() caught in Utils.RawResourceToBuffer()");//$NON-NLS-1$
			}

			return null;
		}
	}

	/**
	 * Compare BufferA with BufferB and return the result.
	 * 
	 * @param bufferA
	 *            : first buffer
	 * @param offsetA
	 *            : index from which to start
	 * @param bufferB
	 *            : second buffer
	 * @param offsetB
	 *            : index from which to start
	 * @param len
	 *            : number of bytes to compare
	 * @return false when the buffers are different, true if they're the sam
	 */
	public static boolean equals(final byte[] bufferA, final int offsetA, final byte[] bufferB, final int offsetB,
			final int len) {
		if (len < 0) {
			return false;
		}

		if (offsetA < 0 || offsetA > bufferA.length) {
			return false;
		}

		if (offsetB < 0 || offsetB > bufferB.length) {
			return false;
		}

		if (offsetA + len > bufferA.length) {
			return false;
		}

		if (offsetB + len > bufferB.length) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			if (bufferA[offsetA + i] != bufferB[offsetB + i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Search a buffer looking for a token, returns token's index.
	 * 
	 * @param buffer
	 *            : buffer to search
	 * @param token
	 *            : token to look for
	 * @return start position of token into the buffer, -1 if token is not found
	 */
	public static int getIndex(final byte[] buffer, final byte[] token) {
		for (int i = 0; i < buffer.length; i++) {
			if (equals(buffer, i, token, 0, token.length)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Sleep.
	 * 
	 * @param t
	 *            the t
	 */
	public static void sleep(final int t) {
		try {
			Thread.sleep(t);
		} catch (final InterruptedException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " sleep() throwed an exception");//$NON-NLS-1$
			}
			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}
	}

	/** The rand. */
	static SecureRandom rand = new SecureRandom();

	/**
	 * Gets the unique id.
	 * 
	 * @return the unique id
	 */
	public static long getRandom() {
		return rand.nextLong();
	}

	/**
	 * Gets the time stamp.
	 * 
	 * @return the time stamp
	 */
	public static long getTimeStamp() {
		return System.currentTimeMillis();
	}

	/**
	 * Byte array to int.
	 * 
	 * @param buffer
	 *            the buffer
	 * @param offset
	 *            the offset
	 * @return the int
	 */
	public static int byteArrayToInt(final byte[] buffer, final int offset) {
		if (Cfg.DEBUG) {
			Check.requires(buffer.length >= offset + 4, "short buffer"); //$NON-NLS-1$
		}
		try {
			final DataBuffer databuffer = new DataBuffer(buffer, offset, buffer.length - offset);
			final int value = databuffer.readInt();
			return value;
		} catch (final IOException ex) {
			if (Cfg.EXCEPTION) {
				Check.log(ex);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: " + ex.toString());//$NON-NLS-1$
			}
		}

		return 0;

	}

	/**
	 * Int to byte array.
	 * 
	 * @param value
	 *            the value
	 * @return the byte[]
	 */
	public static byte[] intToByteArray(final int value) {
		final byte[] output = new byte[4];
		final DataBuffer buffer = new DataBuffer(output, 0, 4);
		buffer.writeInt(value);

		return output;
	}

	/**
	 * Long to byte array.
	 * 
	 * @param value
	 *            the value
	 * @return the byte[]
	 */
	public static byte[] longToByteArray(final long value) {
		final byte[] output = new byte[8];
		final DataBuffer buffer = new DataBuffer(output, 0, 8);
		buffer.writeLong(value);

		return output;
	}

	/**
	 * Byte array to hex.
	 * 
	 * @param data
	 *            the data
	 * @return the string
	 */
	public static String byteArrayToHex(final byte[] data) {
		return byteArrayToHex(data, 0, data.length);
	}

	/**
	 * Converte un array di byte in una stringa che ne rappresenta il contenuto
	 * in formato esadecimale.
	 * 
	 * @param data
	 *            the data
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @return the string
	 */
	public static String byteArrayToHex(final byte[] data, final int offset, final int length) {
		final StringBuffer buf = new StringBuffer();
		for (int i = offset; i < offset + length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int twohalfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (twohalfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Concatena first e second.
	 * 
	 * @param first
	 *            the first
	 * @param lenFirst
	 *            the len first
	 * @param second
	 *            the second
	 * @param lenSecond
	 *            the len second
	 * @return the byte[]
	 */
	public static byte[] concat(final byte[] first, final int lenFirst, final byte[] second, final int lenSecond) {

		final byte[] sum = new byte[lenFirst + lenSecond];
		System.arraycopy(first, 0, sum, 0, lenFirst);
		System.arraycopy(second, 0, sum, lenFirst, lenSecond);

		return sum;
	}

	/**
	 * Concat.
	 * 
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 * @return the byte[]
	 */
	public static byte[] concat(final byte[] first, final byte[] second) {
		return concat(first, first.length, second, second.length);
	}

	/**
	 * Restituisce una copia della parte dell'array specificata.
	 * 
	 * @param payload
	 *            the payload
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	// COMPAT
	public static byte[] copy(final byte[] payload, final int offset, final int length) {
		final byte[] buffer = new byte[length];
		System.arraycopy(payload, offset, buffer, 0, length);
		return buffer;
	}

	/**
	 * Duplicate array.
	 * 
	 * @param ct
	 *            the ct
	 * @return the byte[]
	 */
	// COMPAT
	public static byte[] copy(final byte[] ct) {
		return copy(ct, 0, ct.length);
	}

	/**
	 * Restituisce la codifica default del messaggio paddato di zeri per la
	 * lunghezza specificata.
	 * 
	 * @param byteAddress
	 *            the byte address
	 * @param len
	 *            the len
	 * @return the byte[]
	 */
	public static byte[] padByteArray(final byte[] byteAddress, final int len) {
		final byte[] padAddress = new byte[len];
		System.arraycopy(byteAddress, 0, padAddress, 0, Math.min(len, byteAddress.length));
		if (Cfg.DEBUG) {
			Check.ensures(padAddress.length == len, "padByteArray wrong len: " + padAddress.length); //$NON-NLS-1$
		}
		return padAddress;
	}

	/**
	 * Chomp.
	 * 
	 * @param sd
	 *            the sd
	 * @param c
	 *            the c
	 * @return the string
	 */
	public static String chomp(final String sd, final String c) {
		if (sd == null) {
			return null;
		}
		if (sd.length() == 0) {
			return ""; //$NON-NLS-1$
		}
		if (sd.endsWith(c)) {
			return sd.substring(0, sd.length() - c.length());
		}

		return sd;
	}

	/**
	 * Unspace.
	 * 
	 * @param string
	 *            the string
	 * @return the string
	 */
	public static String unspace(final String string) {
		if (Cfg.DEBUG) {
			Check.requires(string != null, "Unspace: null string"); //$NON-NLS-1$
		}
		if (string == null) {
			return null;
		}
		final StringBuffer unspace = new StringBuffer();
		int spaces = 0;
		final int len = string.length();
		for (int i = 0; i < len; i++) {
			final char c = string.charAt(i);
			if (c != ' ') {
				unspace.append(c);
			} else {
				spaces++;
			}
		}
		if (Cfg.DEBUG) {
			Check.ensures(unspace.length() + spaces == string.length(), "Unspace: wrong spaces"); //$NON-NLS-1$
		}
		return unspace.toString();
	}

	/**
	 * Byte array to hex string.
	 * 
	 * @param b
	 *            the b
	 * @return the string
	 */
	public static String byteArrayToHexString(final byte[] b) {
		final StringBuffer sb = new StringBuffer(b.length * 2);

		for (final byte element : b) {
			final int v = element & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Hex string to byte array.
	 * 
	 * @param s
	 *            the s
	 * @return the byte[]
	 */
	public static byte[] hexStringToByteArray(final String s, int offset, int len) {
		final byte[] b = new byte[len / 2];

		for (int i = 0; i < b.length; i++) {
			final int index = offset + i * 2;
			final int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static byte[] hexStringToByteArray(final String string) {
		return hexStringToByteArray(string, 0, string.length());
	}

	/**
	 * Hex string to byte array2.
	 * 
	 * @param config
	 *            the config
	 * @return the byte[]
	 */
	public static byte[] hexStringToByteArray2(final String config) {

		final int offset = 4;
		final int len = config.length() / 2;
		final byte[] ret = new byte[len + 4];

		for (int i = offset; i < ret.length; i++) {
			final char first = config.charAt((i - offset) * 2);
			final char second = config.charAt((i - offset) * 2 + 1);

			int value = Integer.parseInt(new String(new byte[] { (byte) first }), 16) << 4;
			value += second;

			ret[i] = (byte) Integer.parseInt(new String(new byte[] { (byte) second }), 16);
		}

		final DataBuffer databuffer = new DataBuffer(ret, 0, 4);
		databuffer.writeInt(len);

		return ret;
	}

	public static byte[] hexStringToByteArray2(final String wchar, int offset, int len) {

		final byte[] ret = new byte[len / 2];

		for (int i = 0; i < ret.length; i++) {
			final char first = wchar.charAt(offset + (i * 2));
			final char second = wchar.charAt(offset + (i * 2 + 1));

			// int value = NumberUtilities.hexDigitToInt(first) << 4;
			// value += NumberUtilities.hexDigitToInt(second);
			int value = Integer.parseInt(new String(new byte[] { (byte) first }), 16) << 4;
			value += second;

			// #ifdef DBC
			if (Cfg.DEBUG) {
				Check.asserts(value >= 0 && value < 256, "HexStringToByteArray: wrong value"); //$NON-NLS-1$
				// #endif
			}

			ret[i] = (byte) value;
		}

		return ret;
	}

	public static boolean matchStar(String wildcardProcess, String processName) {

		if (processName == null) {
			return (wildcardProcess == null);
		}

		for (;;) {
			if (wildcardProcess.length() == 0) {
				return (processName.length() == 0);
			}

			if (wildcardProcess.charAt(0) == '*') {
				wildcardProcess = wildcardProcess.substring(1);

				if (wildcardProcess.length() == 0) {
					return true;
				}

				if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != '*') {
					final int len = processName.length();

					for (int i = 0; i < len; i++) {
						final char c = processName.charAt(0);

						processName = processName.substring(1);
						final String tp = wildcardProcess.substring(1);

						if (c == wildcardProcess.charAt(0) && matchStar(tp, processName)) {
							return true;
						}
					}

					return false;
				}

				for (int i = 0; i < processName.length(); i++) {
					processName = processName.substring(1);

					if (matchStar(wildcardProcess, processName)) {
						return true;
					}
				}

				return false;
			}

			if (processName.length() == 0) {
				return false;
			}

			if (wildcardProcess.charAt(0) != '?' && wildcardProcess.charAt(0) != processName.charAt(0)) {
				return false;
			}

			processName = processName.substring(1);
			wildcardProcess = wildcardProcess.substring(1);
		}
		// NOTREACHED
	}

	public static String inputStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
