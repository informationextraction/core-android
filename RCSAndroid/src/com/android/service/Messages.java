package com.android.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.android.service.auto.Cfg;
import com.android.service.util.Check;
import com.android.service.util.Utils;

public class Messages {
	private static final String TAG = "Messages";
	
	private static HashMap<String, String> messages;
	private static boolean initialized;

	private Messages() {
	}

	public synchronized static boolean init(Context context) {
		if (initialized) {
			return true;
		}

		try {
			messages = new HashMap<String, String>();
			Resources resources = context.getResources();
			InputStream stream = resources.openRawResource(R.raw.messages);

			BufferedReader br = new BufferedReader(new InputStreamReader(stream));

			// byte[] raw = new byte[stream.available()];
			// stream.read(raw, 0, raw.length);

			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}

				String[] kv = line.split("="); //$NON-NLS-1$
				if (Cfg.DEBUG) {
					Check.asserts(kv.length == 2, "wrong number of tokens"); //$NON-NLS-1$
					Check.log(TAG + " " + kv[0] + " " + kv[1]); //$NON-NLS-1$ //$NON-NLS-2$
				}

				messages.put(kv[0], kv[1]);
				
				if (Cfg.DEBUG) {
					Check.asserts(messages.containsKey(kv[0]),"strange hashmap behaviour"); //$NON-NLS-1$
				}
			}

			initialized = true;
		} catch (Exception ex) {
			return false;
		}
		return true;

	}

	public static String getString(String key) {
		if (!initialized) {
			init(Status.getAppContext());
		}
		try {
			if (Cfg.DEBUG) {
				Check.asserts(messages.containsKey(key),"strange hashmap behaviour"); //$NON-NLS-1$
			}
			String str = messages.get(key);
			return str;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
