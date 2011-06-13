package com.android.service;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "com.android.service.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
			String str = RESOURCE_BUNDLE.getString(key);
			return str;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
