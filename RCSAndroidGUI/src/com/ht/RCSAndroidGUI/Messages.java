/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSAndroid
 * File         : Messages.java
 * Created      : Apr 18, 2011
 * Author		: zeno
 * *******************************************/
package com.ht.RCSAndroidGUI;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

// TODO: Auto-generated Javadoc
/**
 * The Class Messages.
 */
public class Messages {
	
	/** The Constant BUNDLE_NAME. */
	private static final String BUNDLE_NAME = "com.ht.RCSAndroidGUI.messages"; //$NON-NLS-1$

	/** The Constant RESOURCE_BUNDLE. */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * Instantiates a new messages.
	 */
	private Messages() {
	}

	/**
	 * Gets the string.
	 *
	 * @param key the key
	 * @return the string
	 */
	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
