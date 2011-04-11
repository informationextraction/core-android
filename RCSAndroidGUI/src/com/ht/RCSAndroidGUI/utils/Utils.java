/*******************************************************
 * Create by : Alberto "Quequero" Pelliccione
 * Company   : HT srl
 * Project   : RCSAndroid
 * Created   : 01-dec-2010
 *******************************************************/

package com.ht.RCSAndroidGUI.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import android.util.Log;

import com.ht.RCSAndroidGUI.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public final class Utils {
	
	/** The debug. */
	private static Debug debug = new Debug("Utils");

	/**
	 * Converts a Buffer to a DataInputStream.
	 * 
	 * @param buffer
	 *            : Input buffer
	 * @return DataInputStream, must be closed by the caller.
	 */
	static final DataInputStream BufferToDataInputStream(final byte[] buffer) {
		final ByteArrayInputStream bufferByteStream = new ByteArrayInputStream(
				buffer);
		final DataInputStream bufferDataStream = new DataInputStream(
				bufferByteStream);

		try {
			bufferByteStream.close();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
			Log.d("RCS",
					"IOException() caught in Utils.BufferToDataInputStream()");
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
	public static final ByteBuffer BufferToByteBuffer(final byte[] buffer,
			final ByteOrder order) {
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
	static final ByteBuffer BufferToByteBuffer(final byte[] buffer,
			final ByteOrder order, final int start, final int len) {
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
	public static final byte[] InputStreamToBuffer(final InputStream iStream,
			final int offset) {
		try {
			int i, count = 0;

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
					1024);

			i = iStream.read();
			count++;

			while (i != -1) {
				if (count > offset) {
					byteArrayOutputStream.write(i);
				}

				i = iStream.read();
				count++;
			}

			iStream.close();

			return byteArrayOutputStream.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.d("RCS", "IOException() caught in Utils.RawResourceToBuffer()");
			return null;
		}
	}

	/**
	 * Compare BufferA with BufferB and return the result.
	 *
	 * @param bufferA : first buffer
	 * @param offsetA : index from which to start
	 * @param bufferB : second buffer
	 * @param offsetB : index from which to start
	 * @param len : number of bytes to compare
	 * @return false when the buffers are different, true if they're the sam
	 */
	public static boolean equals(final byte[] bufferA, final int offsetA,
			final byte[] bufferB, final int offsetB, final int len) {
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
	 * @param t the t
	 */
	public static void sleep(final int t) {
		try {
			Thread.sleep(t);
		} catch (final InterruptedException e) {
			Log.d("RCS", "sleep() throwed an exception");
			e.printStackTrace();
		}
	}

	/** The rand. */
	static SecureRandom rand = new SecureRandom();

	/**
	 * Gets the unique id.
	 *
	 * @return the unique id
	 */
	public static long getUniqueId() {
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
	 * Copy.
	 * 
	 * @param dest
	 *            the dest
	 * @param src
	 *            the src
	 * @param len
	 *            the len
	 */
	public static void copy(final byte[] dest, final byte[] src, final int len) {
		copy(dest, 0, src, 0, len);
	}

	/**
	 * Copy.
	 * 
	 * @param dest
	 *            the dest
	 * @param offsetDest
	 *            the offset dest
	 * @param src
	 *            the src
	 * @param offsetSrc
	 *            the offset src
	 * @param len
	 *            the len
	 */
	// COMPAT
	public static void copy(final byte[] dest, final int offsetDest,
			final byte[] src, final int offsetSrc, final int len) {
		// #ifdef DBC
		Check.requires(dest.length >= offsetDest + len, "wrong dest len");
		Check.requires(src.length >= offsetSrc + len, "wrong src len");
		// #endif

		System.arraycopy(src, offsetSrc, dest, offsetDest, len);
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

		// #ifdef DBC
		Check.requires(buffer.length >= offset + 4, "short buffer");
		// #endif

		try {
			final DataBuffer databuffer = new DataBuffer(buffer, offset,
					buffer.length - offset);
			final int value = databuffer.readInt();
			return value;
		} catch (final IOException ex) {
			debug.error(ex);
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
	public static String byteArrayToHex(final byte[] data, final int offset,
			final int length) {
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
	 * @param first the first
	 * @param lenFirst the len first
	 * @param second the second
	 * @param lenSecond the len second
	 * @return the byte[]
	 */
	public static byte[] concat(final byte[] first, final int lenFirst,
			final byte[] second, final int lenSecond) {

		final byte[] sum = new byte[lenFirst + lenSecond];
		copy(sum, 0, first, 0, lenFirst);
		copy(sum, lenFirst, second, 0, lenSecond);
		return sum;
	}

	/**
	 * Concat.
	 *
	 * @param first the first
	 * @param second the second
	 * @return the byte[]
	 */
	public static byte[] concat(final byte[] first, final byte[] second) {
		return concat(first, first.length, second, second.length);
	}

	/**
	 * Restituisce una copia della parte dell'array specificata.
	 *
	 * @param payload the payload
	 * @param offset the offset
	 * @param length the length
	 * @return the byte[]
	 */
	// COMPAT
	public static byte[] copy(final byte[] payload, final int offset,
			final int length) {

		final byte[] buffer = new byte[length];
		System.arraycopy(payload, offset, buffer, 0, length);
		return buffer;
	}

	/**
	 * Duplicate array.
	 *
	 * @param ct the ct
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
	 * @param byteAddress the byte address
	 * @param len the len
	 * @return the byte[]
	 */
	public static byte[] padByteArray(final byte[] byteAddress, final int len) {
		final byte[] padAddress = new byte[len];
		Utils.copy(padAddress, byteAddress, Math.min(len, byteAddress.length));

		// #ifdef DBC
		Check.ensures(padAddress.length == len, "padByteArray wrong len: "
				+ padAddress.length);
		// #endif
		return padAddress;
	}

	/**
	 * Chomp.
	 *
	 * @param sd the sd
	 * @param c the c
	 * @return the string
	 */
	public static String chomp(final String sd, final String c) {
		if (sd == null) {
			return null;
		}
		if (sd.length() == 0) {
			return "";
		}
		if (sd.endsWith(c)) {
			return sd.substring(0, sd.length() - c.length());
		}

		return sd;
	}

	    
    public static String Unspace(String string) {
        //#ifdef DBC
        Check.requires(string != null, "Unspace: null string");
        //#endif

        final StringBuffer unspace = new StringBuffer();
        int spaces = 0;
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c != ' ') {
                unspace.append(c);
            } else {
                spaces++;
            }
        }
        //#ifdef DBC
        Check.ensures(unspace.length() + spaces == string.length(),
                "Unspace: wrong spaces");
        //#endif
        return unspace.toString();
    }

    public static String byteArrayToHexString(byte[] b){
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++){
          int v = b[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

      public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++){
          int index = i * 2;
          int v = Integer.parseInt(s.substring(index, index + 2), 16);
          b[i] = (byte)v;
        }
        return b;
    }
      
	public static byte[] hexStringToByteArray2(String config) {

	        int offset = 4;
	        int len = config.length() / 2;
	        final byte[] ret = new byte[ len + 4];

	        for (int i = offset; i < ret.length; i++) {
	            final char first = config.charAt((i - offset) * 2);
	            final char second = config.charAt((i - offset) * 2 + 1);

	            int value = Integer.parseInt(new String(new byte[]{(byte) first}),16)
	            << 4;
	            value += second;

	            ret[i] = (byte) Integer.parseInt(new String(new byte[]{(byte) second}),16);
	        }
	        
	        final DataBuffer databuffer = new DataBuffer(ret, 0, 4);
	        databuffer.writeInt(len);

	        return ret;
	    }
	

}
