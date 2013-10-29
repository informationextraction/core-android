package com.android.deviceinfo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.android.deviceinfo.auto.Cfg;

public class ByteArray {
	private static final String TAG = "ByteArray";
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

	public static long byteArrayToLong(final byte[] buffer, final int offset) {
		if (Cfg.DEBUG) {
			Check.requires(buffer.length >= offset + 8, "short buffer"); //$NON-NLS-1$
		}
		try {
			final DataBuffer databuffer = new DataBuffer(buffer, offset, buffer.length - offset);
			final long value = databuffer.readLong();
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
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

    public static byte[] hexStringToByteArray(final String s, int offset,
            int len) {

	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i + offset), 16) << 4)
	                             + Character.digit(s.charAt(i + offset + 1), 16));
	    }
	    return data;
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

}
