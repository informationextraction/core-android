package com.android.networking.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;

public class Digest {
	/**
	 * Calcola il SHA1 del messaggio, usando la crypto api.
	 * 
	 * @param message
	 *            the message
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	public static byte[] SHA1(final byte[] message, final int offset, final int length) {
		MessageDigest digest;
		
		try {
			digest = MessageDigest.getInstance(Messages.getString("19_0")); //$NON-NLS-1$
			digest.update(message, offset, length);
			final byte[] sha1 = digest.digest();

			return sha1;
		} catch (final NoSuchAlgorithmException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
		return null;
	}

	/**
	 * Calcola il MD5 del messaggio, usando la crypto api.
	 * 
	 * @param message
	 *            the message
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	public static byte[] MD5(final byte[] message, final int offset, final int length) {
		MessageDigest digest;
		
		try {
			// TODO: messages
			digest = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			digest.update(message, offset, length);
			final byte[] md5 = digest.digest();

			return md5;
		} catch (final NoSuchAlgorithmException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}
		}
		return null;
	}
	
	/**
	 * SHA-1.
	 * 
	 * @param message
	 *            the message
	 * @return the byte[]
	 */
	public static byte[] SHA1(final byte[] message) {
		return SHA1(message, 0, message.length);
	}
		
	public static String SHA1(final String message) {
		return ByteArray.byteArrayToHex(SHA1(message.getBytes()));
	}
	
	/**
	 * MD5.
	 * 
	 * @param message
	 *            the message
	 * @return the byte[]
	 */
	public static byte[] MD5(final byte[] message) {
		return MD5(message, 0, message.length);
	}
	
	public static String MD5(final String message) {
		return ByteArray.byteArrayToHex(MD5(message.getBytes()));
	}
	
	/**
	 * Standard crc
	 * 
	 * @param packet
	 * @return
	 */
	public static long CRC32(byte[] packet) {
		final CRC32 crc = new CRC32();
		crc.reset();
		crc.update(packet);
		return crc.getValue();
	}
}
