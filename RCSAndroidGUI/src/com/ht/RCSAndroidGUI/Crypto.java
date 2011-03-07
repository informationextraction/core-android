package com.ht.RCSAndroidGUI;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
	// http://www.androidsnippets.org/snippets/39/index.html
	private byte[] aes_key;
	private SecretKeySpec skey_spec;
	private IvParameterSpec ivSpec;
	
	Crypto(byte[] key) {
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
			System.arraycopy(encrypted, offset, buffer, 0, encrypted.length - offset);
			return cipher.doFinal(encrypted);
		}  
	}
}
