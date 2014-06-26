package com.android.dvci;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;

import com.android.dvci.auto.Cfg;
import com.android.dvci.util.ByteArray;
import com.android.dvci.util.Check;
import com.android.dvci.util.Utils;

public class MessagesDecrypt {
	private static final String TAG = "MessageDecrypt";

	final HashMap<String, String> messages = new HashMap<String, String>();

	String pack = Status.self().getAppContext().getPackageName();
	
	public MessagesDecrypt(Context context) {

		if (Cfg.DEBUG) {
			Check.asserts(context != null, " (init) Assert failed");
		}

		try {

			if (Cfg.DEBUG) {
				Check.log(TAG + " (MessagesDecrypt)");
			}

			InputStream stream = Utils.getAssetStream("m.bin");

			final SecretKey key = produceKey(Cfg.RNDMSG);

			if (Cfg.DEBUG) {
				Check.asserts(key != null, "null key"); //$NON-NLS-1$
			}

			if (Cfg.DEBUG) {

				Check.log(TAG + " (init): key=" + ByteArray.byteArrayToHex(key.getEncoded()));
			}

			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //$NON-NLS-1$
			final byte[] iv = new byte[16];
			Arrays.fill(iv, (byte) 0);
			final IvParameterSpec ivSpec = new IvParameterSpec(iv);

			cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
			
			final CipherInputStream cis = new CipherInputStream(stream, cipher);
			final BufferedReader br = new BufferedReader(new InputStreamReader(cis));

			while (true) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}

				final String[] kv = line.split("=", 2);
				if (Cfg.DEBUG) {
					Check.asserts(kv.length == 2, "wrong number of tokens"); //$NON-NLS-1$
					//Check.log(TAG + " " + kv[0] + " " + kv[1]); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				String value = kv[1].replace("$PACK$", pack);
				messages.put(kv[0], value);

				if (Cfg.DEBUG) {
					Check.asserts(messages.containsKey(kv[0]), "strange hashmap behaviour"); //$NON-NLS-1$
				}
			}
			
			if (Cfg.DEBUG) {
				Check.log(TAG + " (MessagesDecrypt), messages.size: " + messages.size());
			}

		} catch (final Exception ex) {
			if (Cfg.EXCEPTION) {
				Check.log(TAG + " Exception");
				Check.log(ex);
			}
		}
	}

	public static SecretKey produceKey(String key) {

		try {
			if (Cfg.DEBUG) {
				Check.log(" key: " + key + " " + key.length()); //$NON-NLS-1$
			}

			final String salt = Cfg.RANDOM;

			final MessageDigest digest = MessageDigest.getInstance("SHA-1");

			for (int i = 0; i < 128; i++) {
				digest.update(salt.getBytes());
				digest.update(key.getBytes());
				digest.update(digest.digest());
			}

			final byte[] sha1 = digest.digest();

			final byte[] aes_key = new byte[16];
			System.arraycopy(sha1, 0, aes_key, 0, aes_key.length);

			final SecretKey secret = new SecretKeySpec(aes_key, "AES");
			if (Cfg.DEBUG) {
				Check.log(" produced key: " + ByteArray.byteArrayToHex(aes_key)); //$NON-NLS-1$
			}

			return secret;
		} catch (final Exception e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			if (Cfg.DEBUG) {
				Check.log(TAG + " " + e); //$NON-NLS-1$
			}

			return null;
		}

	}

	public HashMap<String, String> getMessages() {
		return messages;
	}
}
