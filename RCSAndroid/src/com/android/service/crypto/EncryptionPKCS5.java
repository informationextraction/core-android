/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : EncryptionPKCS5.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import java.util.Arrays;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EncryptionPKCS5.
 */
public class EncryptionPKCS5 extends Encryption {

	/**
	 * Instantiates a new encryption pkc s5.
	 * 
	 * @param key
	 *            the key
	 */
	public EncryptionPKCS5(final byte[] key) {
		super(key);
	}

	/**
	 * Instantiates a new encryption pkc s5.
	 */
	public EncryptionPKCS5() {
		super(Keys.self().getAesKey());
	}

	/** The Constant DIGEST_LENGTH. */
	private static final int DIGEST_LENGTH = 20;
	/** The debug. */
	private static final String TAG = "EncryptionPKCS5"; //$NON-NLS-1$

	/**
	 * Gets the next multiple.
	 * 
	 * @param len
	 *            the len
	 * @return the next multiple
	 */
	@Override
	public int getNextMultiple(final int len) {
		if (Cfg.DEBUG) {
			Check.requires(len >= 0, "len < 0"); //$NON-NLS-1$
		}
		final int newlen = len + (16 - len % 16);
		if (Cfg.DEBUG) {
			Check.ensures(newlen > len, "newlen <= len"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.ensures(newlen % 16 == 0, "Wrong newlen"); //$NON-NLS-1$
		}
		return newlen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Encryption#pad(byte[], int, int)
	 */
	@Override
	protected byte[] pad(final byte[] plain, final int offset, final int len) {
		return pad(plain, offset, len, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.AndroidServiceGUI.crypto.Encryption#decryptData(byte[], int,
	 * int)
	 */
	@Override
	public byte[] decryptData(final byte[] cyphered, final int enclen, final int offset) throws CryptoException {
		// int padlen = cyphered[cyphered.length -1];
		// int plainlen = enclen - padlen;
		if (Cfg.DEBUG) {
			Check.requires(enclen % 16 == 0, "Wrong padding"); //$NON-NLS-1$
		}
		// if(Cfg.DEBUG) Check.requires(enclen >= plainlen, "Wrong plainlen"); //$NON-NLS-1$
		final byte[] paddedplain = new byte[enclen];
		byte[] plain = null;
		int plainlen = 0;
		byte[] iv = new byte[16];

		final byte[] pt = new byte[16];

		final int numblock = enclen / 16;
		for (int i = 0; i < numblock; i++) {
			final byte[] ct = Utils.copy(cyphered, i * 16 + offset, 16);

			crypto.decrypt(ct, pt);
			xor(pt, iv);
			iv = Utils.copy(ct);
			System.arraycopy(pt, 0, paddedplain, i * 16, 16);
		}

		final int padlen = paddedplain[paddedplain.length - 1];

		if (padlen <= 0 || padlen > 16) {
			if (Cfg.DEBUG) {
				Check.log(TAG + " Error: decryptData, wrong padlen: " + padlen) ;//$NON-NLS-1$
			}
			
			throw new CryptoException();
		}

		plainlen = enclen - padlen;
		plain = new byte[plainlen];

		System.arraycopy(paddedplain, 0, plain, 0, plainlen);
		if (Cfg.DEBUG) {
			Check.ensures(plain != null, "null plain"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.ensures(plain.length == plainlen, "wrong plainlen"); //$NON-NLS-1$
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

		final byte[] sha = SHA1(plain);
		final byte[] plainSha = Utils.concat(plain, sha);
		if (Cfg.DEBUG) {
			Check.asserts(sha.length == DIGEST_LENGTH, "sha.length"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.asserts(plainSha.length == plain.length + DIGEST_LENGTH, "plainSha.length"); //$NON-NLS-1$
		}
		return encryptData(plainSha, 0);
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
		final byte[] plainSha = decryptData(cyphered, 0);
		final byte[] plain = Utils.copy(plainSha, 0, plainSha.length - DIGEST_LENGTH);
		final byte[] sha = Utils.copy(plainSha, plainSha.length - DIGEST_LENGTH, DIGEST_LENGTH);
		final byte[] calculatedSha = SHA1(plainSha, 0, plainSha.length - DIGEST_LENGTH);
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
				Check.log(TAG + " Error: decryptDataIntegrity: sha error!") ;//$NON-NLS-1$
			}
			throw new CryptoException();
		}
	}

}
