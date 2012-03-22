package com.android.service.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import com.android.service.Messages;
import com.android.service.auto.Cfg;
import com.android.service.util.Check;

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
			digest = MessageDigest.getInstance(Messages.getString("19.0")); //$NON-NLS-1$
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
